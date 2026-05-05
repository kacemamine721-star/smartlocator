from django.core.management.base import BaseCommand
import json
import os
from api.models import ChargingStation

class Command(BaseCommand):
    help = 'Import EV stations from GeoJSON'

    def handle(self, *args, **kwargs):
        # Path to the geojson file in the Android project
        path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))), 'app', 'src', 'main', 'res', 'raw', 'ev_stations.geojson')
        
        if not os.path.exists(path):
            self.stderr.write(f"File not found: {path}")
            return

        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        created = 0
        for feature in data['features']:
            props = feature['properties']
            coords = feature['geometry']['coordinates']
            station_id = str(props.get('id'))
            
            # Mapping GeoJSON properties to Model fields
            # We use update_or_create to avoid duplicates if run multiple times
            obj, was_created = ChargingStation.objects.update_or_create(
                station_id=station_id,
                defaults={
                    'name': props.get('NAME') or f"Station {station_id}",
                    'latitude': coords[1],
                    'longitude': coords[0],
                    'address': f"Origin: {props.get('ORIGIN', 'Unknown')}",
                    'city': "Tunisia",
                    'availability': props.get('STATUS') or 'Available',
                    'power': props.get('CS_Speed') or 'Unknown',
                    'ports': "Unknown",
                    'network': props.get('ORIGIN') or 'Unknown',
                    'price': "0.0 TND/kWh",
                    'reliability': props.get('report_us') or 'Unknown',
                    'is_favorite': False,
                    'connectors': [], # Default empty list for JSONField
                }
            )
            if was_created:
                created += 1
                
        self.stdout.write(f'Successfully imported {created} stations.')
