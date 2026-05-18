from django.conf import settings
from django.db import models
from django.db.models.signals import post_save
from django.dispatch import receiver

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
    flags = models.IntegerField(default=0) # Number of times flagged as broken
    image = models.ImageField(upload_to='station_images/', null=True, blank=True)
    busy_until = models.DateTimeField(null=True, blank=True)
    
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

class EVVehicle(models.Model):
    vehicle_id = models.CharField(max_length=100, unique=True)
    brand = models.CharField(max_length=100)
    model_name = models.CharField(max_length=100)
    segment = models.CharField(max_length=50)
    battery_capacity_kwh = models.FloatField(null=True, blank=True)
    usable_capacity_kwh = models.FloatField(null=True, blank=True)
    range_wltp_km = models.IntegerField(null=True, blank=True)
    ac_max_power_kw = models.FloatField(null=True, blank=True)
    ac_connector_type = models.CharField(max_length=50, null=True, blank=True)
    ac_phases = models.IntegerField(null=True, blank=True)
    dc_max_power_kw = models.FloatField(null=True, blank=True)
    dc_connector_type = models.CharField(max_length=50, null=True, blank=True)
    km_per_hour_ac = models.IntegerField(null=True, blank=True)
    km_per_hour_dc = models.IntegerField(null=True, blank=True)
    full_charge_time_ac_hours = models.FloatField(null=True, blank=True)
    full_charge_time_dc_minutes = models.IntegerField(null=True, blank=True)
    image = models.ImageField(upload_to='ev_images/', null=True, blank=True)

    def __str__(self):
        return f"{self.brand} {self.model_name}"


class UserProfile(models.Model):
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='profile')
    vehicle = models.ForeignKey(EVVehicle, on_delete=models.SET_NULL, null=True, blank=True)
    points = models.IntegerField(default=0)
    current_soc = models.IntegerField(default=65)

    @property
    def badge(self):
        if self.points < 50:
            return "🌱 Rookie Eco-Driver"
        elif self.points < 150:
            return "⚡ Power Charger"
        else:
            return "🏆 Master Navigator"

    def __str__(self):
        return f"Profile of {self.user.username}"

class CheckIn(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="checkins")
    station = models.ForeignKey(ChargingStation, on_delete=models.CASCADE, related_name="checkins")
    is_charging = models.BooleanField(default=True) # True = Busy, False = Available (Leaving)
    timestamp = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-timestamp"]

    def __str__(self):
        status = "Started charging" if self.is_charging else "Finished charging"
        return f"{self.user.username} - {status} at {self.station.name}"


@receiver(post_save, sender=settings.AUTH_USER_MODEL)
def create_user_profile(sender, instance, created, **kwargs):
    if created:
        UserProfile.objects.create(user=instance)

@receiver(post_save, sender=settings.AUTH_USER_MODEL)
def save_user_profile(sender, instance, **kwargs):
    UserProfile.objects.get_or_create(user=instance)

