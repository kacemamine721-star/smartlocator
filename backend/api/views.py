from rest_framework import generics, permissions, viewsets
from rest_framework_simplejwt.views import TokenObtainPairView

from .models import ChargingStation, ContributedStation, Favorite, HistorySession, StationRating, CommunityAlert
from .serializers import (
    ChargingStationSerializer,
    ContributedStationSerializer,
    FavoriteSerializer,
    HistorySessionSerializer,
    RegisterSerializer,
    StationRatingSerializer,
    CommunityAlertSerializer,
)

class ChargingStationViewSet(viewsets.ModelViewSet):
    serializer_class = ChargingStationSerializer
    permission_classes = [permissions.AllowAny]
    authentication_classes = [] 

    def get_queryset(self):
        queryset = ChargingStation.objects.all()
        city = self.request.query_params.get('city')
        if city is not None:
            queryset = queryset.filter(city__iexact=city)
        return queryset


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
        serializer.save(user=self.request.user)


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
