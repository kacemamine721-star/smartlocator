from rest_framework import generics, permissions, viewsets
from rest_framework_simplejwt.views import TokenObtainPairView
from rest_framework.decorators import action
from rest_framework.response import Response

from .models import ChargingStation, ContributedStation, Favorite, HistorySession, StationRating, CommunityAlert, EVVehicle, CheckIn
from django.contrib.auth.models import User
from .serializers import (
    ChargingStationSerializer,
    ContributedStationSerializer,
    FavoriteSerializer,
    HistorySessionSerializer,
    RegisterSerializer,
    StationRatingSerializer,
    CommunityAlertSerializer,
    EVVehicleSerializer,
    UserMeSerializer,
    UserProfileSerializer,
)

class ChargingStationViewSet(viewsets.ModelViewSet):
    serializer_class = ChargingStationSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        from django.utils import timezone
        ChargingStation.objects.filter(availability="Busy", busy_until__lt=timezone.now()).update(availability="Available")
        
        queryset = ChargingStation.objects.all()
        city = self.request.query_params.get('city')
        if city is not None:
            queryset = queryset.filter(city__iexact=city)
        return queryset

    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAuthenticated])
    def check_in(self, request, pk=None):
        station = self.get_object()
        action_type = request.data.get('action', 'start') # 'start' or 'stop'
        
        profile = request.user.profile
        
        if action_type == 'start':
            CheckIn.objects.create(user=request.user, station=station, is_charging=True)
            station.availability = "Busy"
            
            # Calculate duration
            import re
            from django.utils import timezone
            import datetime
            
            duration_minutes = 30 # Default fallback
            energy_needed = 0
            
            vehicle = profile.vehicle
            if vehicle:
                power_val = 0
                match = re.search(r'(\d+)', station.power)
                if match:
                    power_val = float(match.group(1))
                
                if power_val > 22: # Fast
                    charging_power = min(power_val, vehicle.dc_max_power_kw or power_val)
                else:
                    charging_power = min(power_val, vehicle.ac_max_power_kw or power_val)
                
                capacity = vehicle.usable_capacity_kwh or vehicle.battery_capacity_kwh or 50
                energy_needed = capacity * 0.6
                
                if charging_power > 0:
                    duration_minutes = int((energy_needed / charging_power) * 60)
            
            station.busy_until = timezone.now() + datetime.timedelta(minutes=duration_minutes)
            station.save()
            
            # Add to history
            HistorySession.objects.create(
                user=request.user,
                station=station,
                route_only=False,
                kwh_charged=energy_needed,
                duration_min=duration_minutes
            )
            
            profile.points += 10
            profile.save()
            return Response({
                "status": "checked_in", 
                "points": profile.points, 
                "station_status": "Busy",
                "busy_until": station.busy_until,
                "duration_minutes": duration_minutes,
                "energy_needed": energy_needed
            })
            
        elif action_type == 'stop':
            CheckIn.objects.create(user=request.user, station=station, is_charging=False)
            station.availability = "Available"
            station.busy_until = None
            station.save()
            profile.points += 5
            profile.save()
            return Response({"status": "checked_out", "points": profile.points, "station_status": "Available"})
            
        return Response({"error": "Invalid action"}, status=400)

    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAuthenticated])
    def flag_as_broken(self, request, pk=None):
        station = self.get_object()
        station.flags += 1
        if station.flags >= 3:
            station.availability = "Offline"
        station.save()
        return Response({"status": "flagged", "flags": station.flags, "station_status": station.availability})

    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAdminUser],
            parser_classes=[__import__('rest_framework.parsers', fromlist=['MultiPartParser']).MultiPartParser])
    def upload_image(self, request, pk=None):
        station = self.get_object()
        image = request.FILES.get('image')
        if not image:
            return Response({"error": "No image provided"}, status=400)
        station.image = image
        station.save()
        return Response({
            "status": "uploaded",
            "image_url": request.build_absolute_uri(station.image.url)
        })

class RegisterView(generics.CreateAPIView):
    serializer_class = RegisterSerializer
    permission_classes = [permissions.AllowAny]


class LoginView(TokenObtainPairView):
    permission_classes = [permissions.AllowAny]


class FavoriteViewSet(viewsets.ModelViewSet):
    serializer_class = FavoriteSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Favorite.objects.filter(user=self.request.user).select_related("station")

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


class HistorySessionViewSet(viewsets.ModelViewSet):
    serializer_class = HistorySessionSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return HistorySession.objects.filter(user=self.request.user).select_related("station")

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


class StationRatingViewSet(viewsets.ModelViewSet):
    serializer_class = StationRatingSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return StationRating.objects.filter(user=self.request.user).select_related("station")

    def perform_create(self, serializer):
        station = serializer.validated_data['station']
        user = self.request.user
        rating = StationRating.objects.filter(user=user, station=station).first()
        if rating:
            serializer.instance = rating
            serializer.save()
        else:
            serializer.save(user=user)


class ContributedStationViewSet(viewsets.ModelViewSet):
    serializer_class = ContributedStationSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ContributedStation.objects.filter(submitted_by=self.request.user)

    def perform_create(self, serializer):
        serializer.save(submitted_by=self.request.user)


class CommunityAlertViewSet(viewsets.ModelViewSet):
    serializer_class = CommunityAlertSerializer
    permission_classes = [permissions.IsAuthenticatedOrReadOnly]
    
    def get_authenticators(self):
        if self.request.method == 'GET':
            return []
        return super().get_authenticators()

    def get_queryset(self):
        # Admins see all, users see only active/validated alerts
        if self.request.user.is_staff:
            return CommunityAlert.objects.all()
        return CommunityAlert.objects.filter(is_active=True)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

class EVVehicleViewSet(viewsets.ModelViewSet):
    serializer_class = EVVehicleSerializer
    permission_classes = [permissions.IsAuthenticatedOrReadOnly]
    queryset = EVVehicle.objects.all()

class UserMeView(generics.RetrieveUpdateAPIView):
    serializer_class = UserMeSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_object(self):
        return self.request.user

    def update(self, request, *args, **kwargs):
        # Allow updating profile's vehicle_id directly via this endpoint
        user = self.get_object()
        vehicle_id = request.data.get('vehicle_id')
        if vehicle_id is not None:
            profile = user.profile
            if vehicle_id == "":
                profile.vehicle = None
            else:
                try:
                    vehicle = EVVehicle.objects.get(id=vehicle_id)
                    profile.vehicle = vehicle
                except EVVehicle.DoesNotExist:
                    pass
            profile.save()
        return super().update(request, *args, **kwargs)

