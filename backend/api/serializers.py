from rest_framework import serializers
from .models import ChargingStation

class ChargingStationSerializer(serializers.ModelSerializer):
    class Meta:
        model = ChargingStation
        fields = '__all__'
