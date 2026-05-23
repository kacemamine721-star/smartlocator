from django.db import models
from django.contrib.auth.models import User
from stations.models import ChargingStation

class StationRating(models.Model):
    user    = models.ForeignKey(User, on_delete=models.CASCADE)
    station = models.ForeignKey(ChargingStation, on_delete=models.CASCADE)
    rating  = models.IntegerField()           # 1–5
    comment = models.TextField(blank=True)
    visited_at = models.DateTimeField(auto_now_add=True)
    class Meta:
        unique_together = ('user', 'station')
