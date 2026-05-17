import json
import os
from django.core.management.base import BaseCommand
from django.core.files import File
from api.models import EVVehicle

class Command(BaseCommand):
    help = 'Import EV vehicles from JSON and images'

    def handle(self, *args, **kwargs):
        json_path = os.path.join('..', 'app', 'src', 'main', 'res', 'raw', 'ev_database_tunisia.json')
        img_dir = os.path.join('..', 'app', 'src', 'main', 'assets', 'ev_images')
        
        if not os.path.exists(json_path):
            self.stderr.write(self.style.ERROR(f'Could not find {json_path}'))
            return

        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        created_count = 0
        updated_count = 0

        for item in data:
            vehicle_id = item.get('id')
            defaults = {
                'brand': item.get('brand', ''),
                'model_name': item.get('model', ''),
                'segment': item.get('segment', ''),
                'battery_capacity_kwh': item.get('battery_capacity_kwh'),
                'usable_capacity_kwh': item.get('usable_capacity_kwh'),
                'range_wltp_km': item.get('range_wltp_km'),
                'ac_max_power_kw': item.get('ac_max_power_kw'),
                'ac_connector_type': item.get('ac_connector_type'),
                'ac_phases': item.get('ac_phases'),
                'dc_max_power_kw': item.get('dc_max_power_kw'),
                'dc_connector_type': item.get('dc_connector_type'),
                'km_per_hour_ac': item.get('km_per_hour_ac'),
                'km_per_hour_dc': item.get('km_per_hour_dc'),
                'full_charge_time_ac_hours': item.get('full_charge_time_ac_hours'),
                'full_charge_time_dc_minutes': item.get('full_charge_time_dc_minutes'),
            }

            obj, created = EVVehicle.objects.update_or_create(
                vehicle_id=vehicle_id,
                defaults=defaults
            )

            # Try to attach image
            # The image file names in ev_images are like "Brand_Model.png" or "Brand_Model_Name.png".
            # We will search the directory for a match.
            brand_safe = str(item.get('brand')).replace(' ', '_').replace('-', '_')
            model_safe = str(item.get('model')).replace(' ', '_').replace('-', '_')
            
            # Simple matching logic based on id or brand/model combinations
            potential_names = [
                f"{item.get('brand')}_{item.get('model')}.png".replace(' ', '_'),
                f"{item.get('brand')}_{item.get('model')}.png".replace(' ', '_').replace('-', '_'),
                f"{item.get('brand')}_{item.get('model')}.png".replace(' ', '_').replace('&', 'and'),
                f"{item.get('model')}.png".replace(' ', '_'), # Fallback to just model name
                f"{item.get('model')}.png".replace(' ', '_').replace('-', '_')
            ]
            
            img_found = False
            if not obj.image and os.path.exists(img_dir):
                for fname in os.listdir(img_dir):
                    # Replace spaces and hyphens with underscores in both for better matching
                    fname_clean = fname.lower().replace(' ', '_').replace('-', '_')
                    if fname_clean in [n.lower().replace('-', '_') for n in potential_names]:
                        img_path = os.path.join(img_dir, fname)
                        with open(img_path, 'rb') as img_f:
                            obj.image.save(fname, File(img_f), save=True)
                        img_found = True
                        break

            if created:
                created_count += 1
            else:
                updated_count += 1

        self.stdout.write(self.style.SUCCESS(f'Successfully processed vehicles. Created: {created_count}, Updated: {updated_count}.'))
