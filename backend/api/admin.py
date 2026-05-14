from django.contrib import admin

from .models import ChargingStation, ContributedStation, Favorite, HistorySession, StationRating

admin.site.register(ChargingStation)
admin.site.register(Favorite)
admin.site.register(HistorySession)
admin.site.register(StationRating)
admin.site.register(ContributedStation)

# Register your models here.
