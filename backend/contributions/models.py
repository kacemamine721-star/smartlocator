from django.contrib.gis.db import models
from django.contrib.auth.models import User

class ContributedStation(models.Model):
    submitted_by = models.ForeignKey(User, on_delete=models.CASCADE)
    name         = models.CharField(max_length=200, blank=True)
    location     = models.PointField()
    cs_speed     = models.CharField(max_length=50)
    status       = models.CharField(max_length=50, default='UNKNOWN')
    submitted_at = models.DateTimeField(auto_now_add=True)
    approved     = models.BooleanField(default=False)
