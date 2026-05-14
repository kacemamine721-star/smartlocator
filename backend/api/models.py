from django.conf import settings
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
    cs_speed = models.CharField(max_length=100, blank=True, null=True) # e.g., FAST (50-100 KW)
    connectors = models.JSONField(default=list) # e.g., ["CCS2", "Type 2"]
    
    def __str__(self):
        return f"{self.name} ({self.city})"


class Favorite(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="favorites")
    station = models.ForeignKey(ChargingStation, on_delete=models.CASCADE, related_name="favorited_by")
    saved_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["user", "station"], name="unique_user_station_favorite")
        ]
        ordering = ["-saved_at"]

    def __str__(self):
        return f"{self.user} -> {self.station}"


class HistorySession(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="history_sessions")
    station = models.ForeignKey(ChargingStation, on_delete=models.CASCADE, related_name="history_sessions")
    route_only = models.BooleanField(default=True)
    kwh_charged = models.FloatField(default=0)
    duration_min = models.PositiveIntegerField(default=0)
    visited_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-visited_at"]

    def __str__(self):
        return f"{self.user} visited {self.station}"


class StationRating(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="station_ratings")
    station = models.ForeignKey(ChargingStation, on_delete=models.CASCADE, related_name="ratings")
    stars = models.PositiveSmallIntegerField()
    comment = models.TextField(blank=True)
    rated_at = models.DateTimeField(auto_now=True)

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["user", "station"], name="unique_user_station_rating")
        ]
        ordering = ["-rated_at"]

    def __str__(self):
        return f"{self.stars}/5 for {self.station} by {self.user}"


class ContributedStation(models.Model):
    submitted_by = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="contributed_stations")
    name = models.CharField(max_length=200)
    latitude = models.FloatField()
    longitude = models.FloatField()
    speed = models.CharField(max_length=50)
    status = models.CharField(max_length=50, default="Unknown")
    approved = models.BooleanField(default=False)
    submitted_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-submitted_at"]

    def __str__(self):
        return f"{self.name} submitted by {self.submitted_by}"

    def save(self, *args, **kwargs):
        # Check if it was already approved
        old_instance = ContributedStation.objects.filter(pk=self.pk).first()
        is_new_approval = self.approved and (old_instance is None or not old_instance.approved)
        
        super().save(*args, **kwargs)
        
        if is_new_approval:
            # Automatically create a real ChargingStation
            # Use numeric ID to avoid mobile app parsing errors (Integer.parseInt)
            ChargingStation.objects.create(
                station_id=f"99{self.pk}", 
                name=self.name,
                address=f"Contribution {self.pk}",
                city="Community",
                availability="Available",
                power=self.speed,
                ports="1 port",
                network="Community",
                price="Free/Community",
                reliability="New",
                latitude=self.latitude,
                longitude=self.longitude,
                connectors=["Type 2"]
            )

class CommunityAlert(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="alerts")
    alert_type = models.CharField(max_length=100)
    description = models.TextField()
    latitude = models.FloatField()
    longitude = models.FloatField()
    is_active = models.BooleanField(default=True)
    is_validated = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at"]

    def __str__(self):
        return f"{self.alert_type} alert by {self.user}"
