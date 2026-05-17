from django.contrib.auth.models import User
from rest_framework import serializers

from .models import ChargingStation, ContributedStation, Favorite, HistorySession, StationRating, CommunityAlert, EVVehicle, UserProfile

class ChargingStationSerializer(serializers.ModelSerializer):
    class Meta:
        model = ChargingStation
        fields = '__all__'


class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=8)

    class Meta:
        model = User
        fields = ("id", "username", "email", "password")

    def create(self, validated_data):
        return User.objects.create_user(
            username=validated_data["username"],
            email=validated_data.get("email", ""),
            password=validated_data["password"],
        )


class FavoriteSerializer(serializers.ModelSerializer):
    station_detail = ChargingStationSerializer(source="station", read_only=True)

    class Meta:
        model = Favorite
        fields = ("id", "station", "station_detail", "saved_at")
        read_only_fields = ("id", "station_detail", "saved_at")


class HistorySessionSerializer(serializers.ModelSerializer):
    station_detail = ChargingStationSerializer(source="station", read_only=True)

    class Meta:
        model = HistorySession
        fields = ("id", "station", "station_detail", "route_only", "kwh_charged", "duration_min", "visited_at")
        read_only_fields = ("id", "station_detail", "visited_at")


class StationRatingSerializer(serializers.ModelSerializer):
    class Meta:
        model = StationRating
        fields = ("id", "station", "stars", "comment", "rated_at")
        read_only_fields = ("id", "rated_at")

    def validate_stars(self, value):
        if value < 1 or value > 5:
            raise serializers.ValidationError("Rating must be between 1 and 5.")
        return value


class ContributedStationSerializer(serializers.ModelSerializer):
    submitted_by = serializers.StringRelatedField(read_only=True)

    class Meta:
        model = ContributedStation
        fields = ("id", "name", "latitude", "longitude", "speed", "status", "approved", "submitted_by", "submitted_at")
        read_only_fields = ("id", "approved", "submitted_by", "submitted_at")

class CommunityAlertSerializer(serializers.ModelSerializer):
    user = serializers.StringRelatedField(read_only=True)

    class Meta:
        model = CommunityAlert
        fields = ("id", "alert_type", "description", "latitude", "longitude", "is_active", "is_validated", "user", "created_at")
        read_only_fields = ("id", "is_validated", "user", "created_at")

class EVVehicleSerializer(serializers.ModelSerializer):
    class Meta:
        model = EVVehicle
        fields = '__all__'

class UserProfileSerializer(serializers.ModelSerializer):
    vehicle = EVVehicleSerializer(read_only=True)
    vehicle_id = serializers.PrimaryKeyRelatedField(
        queryset=EVVehicle.objects.all(), source='vehicle', write_only=True, allow_null=True
    )

    class Meta:
        model = UserProfile
        fields = ('user', 'vehicle', 'vehicle_id')
        read_only_fields = ('user',)

class UserMeSerializer(serializers.ModelSerializer):
    profile = UserProfileSerializer(read_only=True)

    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'profile')
        read_only_fields = ('id', 'username', 'email')

