from django.contrib.gis.db import models

class ChargingStation(models.Model):
    name       = models.CharField(max_length=200, blank=True)
    status     = models.CharField(max_length=50, default='UNKNOWN')
    cs_speed   = models.CharField(max_length=50)
    origin     = models.CharField(max_length=100, blank=True)
    report_us  = models.CharField(max_length=50, blank=True)
    location   = models.PointField()          # PostGIS geometry
    is_user_contributed = models.BooleanField(default=False)
    average_rating = models.FloatField(default=0.0)
    rating_count   = models.IntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        indexes = [models.Index(fields=['status', 'cs_speed'])]

    def __str__(self):
        return f"{self.name} - {self.status}"
