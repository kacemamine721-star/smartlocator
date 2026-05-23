from rest_framework import viewsets
from django.contrib.gis.geos import Point
from django.contrib.gis.db.models.functions import Distance
from .models import ChargingStation
from .serializers import ChargingStationSerializer

class ChargingStationViewSet(viewsets.ModelViewSet):
    serializer_class = ChargingStationSerializer

    def get_queryset(self):
        queryset = ChargingStation.objects.all()
        lat = self.request.query_params.get('lat')
        lng = self.request.query_params.get('lng')
        radius = int(self.request.query_params.get('radius', 50000))  # meters
        
        if lat and lng:
            user_location = Point(float(lng), float(lat), srid=4326)
            queryset = queryset.filter(
                location__dwithin=(user_location, radius)
            ).annotate(
                distance=Distance('location', user_location)
            ).order_by('distance')
            
        # Additional filters
        speed = self.request.query_params.get('speed')
        if speed:
            queryset = queryset.filter(cs_speed=speed)
            
        status = self.request.query_params.get('status')
        if status:
            queryset = queryset.filter(status=status)
            
        return queryset
