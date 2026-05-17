from django.contrib import admin

from .models import ChargingStation, ContributedStation, Favorite, HistorySession, StationRating, CommunityAlert, EVVehicle, UserProfile

admin.site.register(ChargingStation)
admin.site.register(Favorite)
admin.site.register(HistorySession)
admin.site.register(StationRating)
admin.site.register(ContributedStation)
admin.site.register(CommunityAlert)
admin.site.register(EVVehicle)
admin.site.register(UserProfile)

