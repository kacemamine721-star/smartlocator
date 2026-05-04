from rest_framework import viewsets
from .models import ChargingStation
from .serializers import ChargingStationSerializer

class ChargingStationViewSet(viewsets.ModelViewSet):
    queryset = ChargingStation.objects.all()
    serializer_class = ChargingStationSerializer
