from django.core.management.base import BaseCommand
import json
import os
from api.models import ChargingStation

class Command(BaseCommand):
    help = 'Import EV stations from enriched GeoJSON'

    def handle(self, *args, **kwargs):
        path = os.path.join('..', 'app', 'src', 'main', 'res', 'raw', 'ev_stations_tunisia.geojson')
        
        if not os.path.exists(path):
            self.stderr.write(f"File not found: {path}")
            return

        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        created = 0
        updated = 0
        for feature in data['features']:
            props = feature['properties']
            coords = feature['geometry']['coordinates']
            station_id = str(props.get('id'))
            
            # Price
            price_val = "Unknown"
            if props.get('charging_free') is True:
                price_val = "Free"
            elif props.get('charging_free') is False:
                price_val = "Paid"
                
            # Connectors
            connectors_val = props.get('connector_types', [])
            if not isinstance(connectors_val, list):
                connectors_val = ["Type 2"]

            # Power
            power_kw = props.get('power_kw', 0)
            power_str = props.get('CS_Speed') or 'Unknown'
            if power_kw and power_kw > 0:
                power_str = f"{power_kw} kW"

            # City & Address
            city = props.get('city') or 'Tunisia'
            address = props.get('approximate_address') or f"Origin: {props.get('ORIGIN', 'Unknown')}"

            obj, was_created = ChargingStation.objects.update_or_create(
                station_id=station_id,
                defaults={
                    'name': props.get('NAME') or f"Station {station_id}",
                    'latitude': coords[1],
                    'longitude': coords[0],
                    'address': address,
                    'city': city,
                    'availability': props.get('STATUS') or 'Available',
                    'power': power_str,
                    'ports': "Unknown",
                    'network': props.get('network') or props.get('ORIGIN') or 'Unknown',
                    'price': price_val,
                    'reliability': props.get('report_us') or 'Unknown',
                    'is_favorite': False,
                    'connectors': connectors_val,
                    'cs_speed': props.get('CS_Speed') or '',
                }
            )
            if was_created:
                created += 1
            else:
                updated += 1
                
        self.stdout.write(self.style.SUCCESS(f'Imported {created} new, updated {updated} existing stations.'))

