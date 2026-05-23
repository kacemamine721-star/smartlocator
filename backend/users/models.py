from django.db import models
from django.contrib.auth.models import User
from stations.models import ChargingStation

class UserProfile(models.Model):
    user        = models.OneToOneField(User, on_delete=models.CASCADE)
    vehicle     = models.CharField(max_length=100, blank=True)
    total_routes = models.IntegerField(default=0)
    total_kwh   = models.FloatField(default=0.0)

class Favorite(models.Model):
    user    = models.ForeignKey(User, on_delete=models.CASCADE)
    station = models.ForeignKey(ChargingStation, on_delete=models.CASCADE)
    saved_at = models.DateTimeField(auto_now_add=True)
    class Meta:
        unique_together = ('user', 'station')

class HistorySession(models.Model):
    user        = models.ForeignKey(User, on_delete=models.CASCADE)
    station     = models.ForeignKey(ChargingStation, on_delete=models.CASCADE)
    date        = models.DateTimeField(auto_now_add=True)
    kwh_charged = models.FloatField(null=True, blank=True)
    route_only  = models.BooleanField(default=False)
    duration_min = models.IntegerField(null=True)
