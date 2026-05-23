from django.contrib.gis import admin
from .models import ChargingStation

@admin.register(ChargingStation)
class ChargingStationAdmin(admin.GISModelAdmin):
    list_display = ('name', 'status', 'cs_speed', 'average_rating')
    list_filter = ('status', 'cs_speed')
    search_fields = ('name',)
