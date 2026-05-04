from django.db import models

class ChargingStation(models.Model):
    station_id = models.CharField(max_length=100, unique=True)
    name = models.CharField(max_length=200)
    address = models.TextField()
    city = models.CharField(max_length=100)
    distance = models.CharField(max_length=50, blank=True, null=True) # Usually calculated, but matching mock
    travel_time = models.CharField(max_length=50, blank=True, null=True)
    availability = models.CharField(max_length=50) # Available, Busy, Offline
    power = models.CharField(max_length=50) # e.g., 150 kW
    ports = models.CharField(max_length=50) # e.g., 6 ports
    hours = models.CharField(max_length=100, default="Open 24/7")
    network = models.CharField(max_length=100) # e.g., ChargeTN
    price = models.CharField(max_length=50) # e.g., 0.95 TND/kWh
    reliability = models.CharField(max_length=50) # e.g., Reliable 96%
    is_favorite = models.BooleanField(default=False)
    latitude = models.FloatField()
    longitude = models.FloatField()
    connectors = models.JSONField(default=list) # e.g., ["CCS2", "Type 2"]
    
    def __str__(self):
        return f"{self.name} ({self.city})"
