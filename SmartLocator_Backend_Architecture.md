# SmartLocator — Backend Architecture (Django + GeoDjango)

## 1. Why Django + GeoDjango instead of Firebase?

| Criterion | Firebase | Django + GeoDjango ✅ |
|---|---|---|
| Geospatial queries | ❌ None native | ✅ PostGIS: nearest, radius, distance |
| GeoJSON import | Manual script | ✅ Native `LayerMapping` in GeoDjango |
| REST API control | ❌ Firestore rules only | ✅ Full DRF endpoints |
| SQL joins / aggregations | ❌ No SQL | ✅ Full PostgreSQL |
| Auth | Firebase Auth SDK | ✅ JWT (simplejwt) |
| Academic value | Low | ✅ High (real backend) |
| Deployment | Google Cloud | ✅ Render.com (free tier) |
| Routing integration | Manual | ✅ Can proxy OSRM/GraphHopper |

---

## 2. Full Stack Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Android App (Java)                    │
│  Fragment/Activity → ViewModel → Repository → Retrofit   │
│  Room DB (offline cache) ←→ API responses               │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTPS (JSON)
                       ▼
┌─────────────────────────────────────────────────────────┐
│          Django REST Framework (Render.com)              │
│                                                          │
│   /api/stations/          ← list, filter, nearest        │
│   /api/stations/{id}/     ← detail + ratings             │
│   /api/stations/{id}/rate/← post a rating                │
│   /api/contributions/     ← submit new station           │
│   /api/auth/register/     ← create account               │
│   /api/auth/login/        ← get JWT token                │
│   /api/auth/refresh/      ← refresh JWT                  │
│   /api/users/me/          ← profile + stats              │
│   /api/users/me/favorites/← CRUD favorites               │
│   /api/users/me/history/  ← CRUD charge sessions         │
│   /api/route/             ← proxy to OSRM/GraphHopper    │
└──────────────────────┬──────────────────────────────────┘
                       │
              ┌────────▼────────┐
              │  PostgreSQL +   │
              │    PostGIS      │
              │  (Render DB)    │
              └─────────────────┘
```

---

## 3. Django Project Structure

```
smartlocator_backend/
├── manage.py
├── requirements.txt
├── Procfile                        ← for Render deployment
├── render.yaml                     ← Render config
├── smartlocator/
│   ├── settings/
│   │   ├── base.py
│   │   ├── development.py
│   │   └── production.py           ← Render env vars
│   ├── urls.py
│   └── wsgi.py
├── stations/
│   ├── models.py                   ← ChargingStation (PointField)
│   ├── serializers.py
│   ├── views.py
│   ├── urls.py
│   └── management/commands/
│       └── import_geojson.py       ← loads GeoJSON into PostGIS
├── users/
│   ├── models.py                   ← UserProfile, Favorite, HistorySession
│   ├── serializers.py
│   ├── views.py
│   └── urls.py
├── ratings/
│   ├── models.py                   ← StationRating
│   ├── serializers.py
│   └── views.py
├── contributions/
│   ├── models.py                   ← ContributedStation
│   ├── serializers.py
│   └── views.py
└── routing/
    ├── views.py                    ← proxy to GraphHopper
    └── urls.py
```

---

## 4. Key Django Models

```python
# stations/models.py
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

# users/models.py
class UserProfile(models.Model):
    user        = models.OneToOneField(User, on_delete=models.CASCADE)
    vehicle     = models.CharField(max_length=100, blank=True)
    total_routes = models.IntegerField(default=0)
    total_kwh   = models.FloatField(default=0.0)

class Favorite(models.Model):
    user    = models.ForeignKey(User, on_delete=models.CASCADE)
    station = models.ForeignKey(ChargingStation, on_delete=models.CASCADE)
    saved_at = models.DateTimeField(auto_now_add=True)
    class Meta:
        unique_together = ('user', 'station')

class HistorySession(models.Model):
    user        = models.ForeignKey(User, on_delete=models.CASCADE)
    station     = models.ForeignKey(ChargingStation, on_delete=models.CASCADE)
    date        = models.DateTimeField(auto_now_add=True)
    kwh_charged = models.FloatField(null=True, blank=True)
    route_only  = models.BooleanField(default=False)
    duration_min = models.IntegerField(null=True)

# ratings/models.py
class StationRating(models.Model):
    user    = models.ForeignKey(User, on_delete=models.CASCADE)
    station = models.ForeignKey(ChargingStation, on_delete=models.CASCADE)
    rating  = models.IntegerField()           # 1–5
    comment = models.TextField(blank=True)
    visited_at = models.DateTimeField(auto_now_add=True)
    class Meta:
        unique_together = ('user', 'station')

# contributions/models.py
class ContributedStation(models.Model):
    submitted_by = models.ForeignKey(User, on_delete=models.CASCADE)
    name         = models.CharField(max_length=200, blank=True)
    location     = models.PointField()
    cs_speed     = models.CharField(max_length=50)
    status       = models.CharField(max_length=50, default='UNKNOWN')
    submitted_at = models.DateTimeField(auto_now_add=True)
    approved     = models.BooleanField(default=False)
```

---

## 5. Key API Endpoints

### Stations

```
GET  /api/stations/
     ?lat=36.8&lng=10.2&radius=20000   ← PostGIS: stations within 20 km
     ?speed=FAST                        ← filter by speed
     ?status=AVAILABLE                  ← filter by status

GET  /api/stations/{id}/               ← full detail + avg rating

POST /api/stations/{id}/rate/          ← submit rating (auth required)
     { "rating": 4, "comment": "..." }
```

**Nearest stations query (GeoDjango):**
```python
from django.contrib.gis.geos import Point
from django.contrib.gis.db.models.functions import Distance

def get_queryset(self):
    lat = self.request.query_params.get('lat')
    lng = self.request.query_params.get('lng')
    radius = int(self.request.query_params.get('radius', 50000))  # meters
    if lat and lng:
        user_location = Point(float(lng), float(lat), srid=4326)
        return ChargingStation.objects.filter(
            location__dwithin=(user_location, radius)  # PostGIS ST_DWithin
        ).annotate(
            distance=Distance('location', user_location)
        ).order_by('distance')
    return ChargingStation.objects.all()
```

### Auth (JWT)
```
POST /api/auth/register/     { email, password, display_name }
POST /api/auth/login/        { email, password } → { access, refresh }
POST /api/auth/token/refresh/ { refresh } → { access }
```

### User
```
GET  /api/users/me/                   ← profile + stats
PUT  /api/users/me/                   ← update profile
GET  /api/users/me/favorites/         ← list favorites
POST /api/users/me/favorites/         ← add favorite { station_id }
DELETE /api/users/me/favorites/{id}/  ← remove
GET  /api/users/me/history/           ← list sessions
POST /api/users/me/history/           ← log session
```

### Routing (proxy)
```
GET /api/route/?from_lat=&from_lng=&to_lat=&to_lng=
    → proxies to GraphHopper, returns { distance_m, duration_s, polyline }
```

### Contributions
```
POST /api/contributions/    ← submit new station (auth required)
GET  /api/contributions/mine/ ← user's own submissions + status
```

---

## 6. GeoJSON Import Command

```python
# stations/management/commands/import_geojson.py
from django.core.management.base import BaseCommand
from django.contrib.gis.geos import Point
import json, os

class Command(BaseCommand):
    help = 'Import EV stations from GeoJSON'

    def handle(self, *args, **kwargs):
        from stations.models import ChargingStation
        path = os.path.join('data', 'EV_CHARGING_STATIONS_TUNISIA.geojson')
        with open(path) as f:
            data = json.load(f)

        created = 0
        for feature in data['features']:
            props = feature['properties']
            coords = feature['geometry']['coordinates']
            obj, was_created = ChargingStation.objects.update_or_create(
                id=props['id'],
                defaults={
                    'name': props.get('NAME') or f"Station {props['id']}",
                    'status': props.get('STATUS') or 'UNKNOWN',
                    'cs_speed': props.get('CS_Speed', ''),
                    'origin': props.get('ORIGIN', ''),
                    'report_us': props.get('report_us', ''),
                    'location': Point(coords[0], coords[1], srid=4326),
                }
            )
            if was_created:
                created += 1
        self.stdout.write(f'Done. {created} stations imported.')
```

Run with:
```bash
python manage.py import_geojson
```

---

## 7. requirements.txt

```
Django==4.2.11
djangorestframework==3.15.1
djangorestframework-simplejwt==5.3.1
django-cors-headers==4.3.1
psycopg2-binary==2.9.9
GDAL==3.8.4
django-environ==0.11.2
gunicorn==21.2.0
whitenoise==6.6.0
requests==2.31.0
```

---

## 8. Deployment on Render.com (Free Tier)

### Why Render?
- ✅ Free web service (Django) — spins down after 15 min idle, fine for demo
- ✅ Free PostgreSQL (90-day trial, plenty for project)
- ✅ PostGIS extension available
- ✅ Deploy directly from GitHub — push → auto-redeploy
- ✅ Environment variables managed in dashboard

### Step-by-step

**1. `Procfile`**
```
web: gunicorn smartlocator.wsgi --log-file -
release: python manage.py migrate && python manage.py import_geojson
```

**2. `render.yaml`** (Infrastructure as Code)
```yaml
services:
  - type: web
    name: smartlocator-api
    env: python
    buildCommand: "pip install -r requirements.txt"
    startCommand: "gunicorn smartlocator.wsgi"
    envVars:
      - key: DJANGO_SETTINGS_MODULE
        value: smartlocator.settings.production
      - key: SECRET_KEY
        generateValue: true
      - key: DATABASE_URL
        fromDatabase:
          name: smartlocator-db
          property: connectionString

databases:
  - name: smartlocator-db
    databaseName: smartlocator
    user: smartlocator
    plan: free
```

**3. `settings/production.py`**
```python
import environ, dj_database_url

env = environ.Env()

DEBUG = False
ALLOWED_HOSTS = ['smartlocator-api.onrender.com']
DATABASES = {'default': dj_database_url.parse(env('DATABASE_URL'))}

# Enable PostGIS
DATABASES['default']['ENGINE'] = 'django.contrib.gis.db.backends.postgis'

INSTALLED_APPS = [
    'django.contrib.gis',
    # ...
]

STATIC_ROOT = 'staticfiles'
STATICFILES_STORAGE = 'whitenoise.storage.CompressedManifestStaticFilesStorage'
```

**4. Enable PostGIS on Render**
```bash
# In Render's PostgreSQL shell (one-time):
CREATE EXTENSION IF NOT EXISTS postgis;
```

**5. Deploy**
```bash
git push origin main   # Render auto-deploys
```

Your API will be live at: `https://smartlocator-api.onrender.com`

---

## 9. Android — Retrofit Integration

```java
// data/remote/ApiClient.java
public class ApiClient {
    private static final String BASE_URL = "https://smartlocator-api.onrender.com/api/";
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = TokenManager.getToken(); // from SharedPreferences
                    Request req = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
                    return chain.proceed(req);
                })
                .build();
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
}

// data/remote/StationApiService.java
public interface StationApiService {
    @GET("stations/")
    Call<List<ChargingStation>> getStations(
        @Query("lat") double lat,
        @Query("lng") double lng,
        @Query("radius") int radiusMeters
    );

    @GET("stations/{id}/")
    Call<ChargingStation> getStation(@Path("id") int id);

    @POST("stations/{id}/rate/")
    Call<Void> rateStation(@Path("id") int id, @Body RatingRequest body);

    @GET("users/me/favorites/")
    Call<List<Favorite>> getFavorites();

    @POST("users/me/favorites/")
    Call<Favorite> addFavorite(@Body FavoriteRequest body);

    @DELETE("users/me/favorites/{id}/")
    Call<Void> removeFavorite(@Path("id") int id);

    @GET("users/me/history/")
    Call<List<HistorySession>> getHistory();

    @POST("users/me/history/")
    Call<HistorySession> logSession(@Body HistoryRequest body);

    @POST("contributions/")
    Call<Contribution> submitContribution(@Body ContributionRequest body);

    @GET("route/")
    Call<RouteResponse> getRoute(
        @Query("from_lat") double fromLat,
        @Query("from_lng") double fromLng,
        @Query("to_lat") double toLat,
        @Query("to_lng") double toLng
    );
}
```

---

## 10. Decision Summary

| Question | Answer |
|---|---|
| Backend language? | **Django + GeoDjango** |
| Database? | **PostgreSQL + PostGIS** (spatial queries) |
| Auth? | **JWT** via `djangorestframework-simplejwt` |
| Deployment? | **Render.com** (free tier, GitHub auto-deploy) |
| GeoJSON import? | **Django management command** `import_geojson` |
| Routing? | **GraphHopper API** proxied through Django `/api/route/` |
| Android networking? | **Retrofit + OkHttp** with JWT interceptor |
| Local cache? | **Room (SQLite)** — offline-first |
| Download Tunisia OSM? | ❌ No — use GraphHopper public API or self-host OSRM |
| docker-android? | ❌ No — only for CI pipelines |
