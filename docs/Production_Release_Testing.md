# ZidCharge Production Release Testing

Use this checklist before sharing a release APK with testers or deploying the backend.

## 1. Backend Production Checks

- Confirm Render uses `DJANGO_SETTINGS_MODULE=core.settings.production`.
- Confirm Render environment variables are set:
  - `DATABASE_URL` points to Neon.
  - `ALLOWED_HOSTS` includes your Render domain, for example `smartlocator.onrender.com,.onrender.com`.
  - `GRAPHHOPPER_API_KEY` is present.
  - `SECRET_KEY` is set in Render, not committed.
  - Optional: `REDIS_URL` points to Upstash.
  - Optional: `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_BUCKET_NAME`, and `R2_ENDPOINT_URL` point to Cloudflare R2.
- Run migrations on production:
  ```bash
  python manage.py migrate --settings=core.settings.production
  ```
- Create the first production admin user:
  ```bash
  python manage.py createsuperuser --settings=core.settings.production
  ```
- Collect static files for the Django admin:
  ```bash
  python manage.py collectstatic --settings=core.settings.production --noinput
  ```
- Import or update the station and EV catalogues:
  ```bash
  python manage.py import_geojson --settings=core.settings.production
  python manage.py import_ev_json --settings=core.settings.production
  ```
- Optional production shell:
  ```bash
  python manage.py shell --settings=core.settings.production
  ```
- To avoid repeating `--settings`, set the environment variable first:
  ```powershell
  $env:DJANGO_SETTINGS_MODULE="core.settings.production"
  python manage.py migrate
  python manage.py collectstatic --noinput
  ```
- Open these endpoints from a browser or API client:
  - `GET /api/stations/` returns stations without authentication.
  - `GET /api/vehicles/` returns EV vehicles.
  - `GET /api/alerts/` returns public active alerts.
  - `GET /api/route/?from_lat=36.8065&from_lng=10.1815&to_lat=36.8073&to_lng=10.1869` returns `distance_m`, `duration_s`, and `polyline`.

### Production `.env` Shape

Do not commit this file. In Render, prefer dashboard environment variables.

```env
DJANGO_SETTINGS_MODULE=core.settings.production
DATABASE_URL=postgresql://user:password@host/dbname?sslmode=require
ALLOWED_HOSTS=smartlocator.onrender.com,.onrender.com
GRAPHHOPPER_API_KEY=your_graphhopper_key
SECRET_KEY=your_production_secret_key
REDIS_URL=redis://default:password@host:port
R2_ACCESS_KEY=your_access_key
R2_SECRET_KEY=your_secret_key
R2_BUCKET_NAME=your_bucket
R2_ENDPOINT_URL=https://your-account.r2.cloudflarestorage.com
```

### Render Start Commands

Recommended Render build command:
```bash
pip install -r requirements.txt && python manage.py collectstatic --settings=core.settings.production --noinput
```

Recommended Render start command:
```bash
gunicorn core.wsgi:application --bind 0.0.0.0:$PORT
```

Recommended Render release command, if using Render release phase:
```bash
python manage.py migrate --settings=core.settings.production && python manage.py import_geojson --settings=core.settings.production && python manage.py import_ev_json --settings=core.settings.production
```

The repository also contains:

- `backend/Procfile`
- `backend/render.yaml`

You can either configure Render from the dashboard or let Render read `render.yaml`.

### Local `collectstatic` Error Fix

If this fails locally:

```text
No module named 'whitenoise'
```

Install the backend requirements in the same Python environment where you run `manage.py`:

```powershell
cd backend
python -m pip install -r requirements.txt
python manage.py collectstatic --settings=core.settings.production --noinput
```

Render will run `pip install -r requirements.txt` during the build, so this error only appears locally when dependencies are missing.

## 2. Android Release APK Build

In Android Studio:

1. Select `Build > Generate Signed Bundle / APK`.
2. Choose `APK`.
3. Select the `release` build variant.
4. Sign with the production keystore.
5. Install the generated release APK on a real device.

Command-line sanity build:
```powershell
.\gradlew.bat :app:assembleRelease
```

The release APK uses:
```text
https://smartlocator.onrender.com/api/
```

Debug-only routing fallback is disabled in release builds because it is guarded by `BuildConfig.DEBUG`.

## 3. End-to-End User Test Plan

### Authentication

- Register a new user.
- Log in.
- Close and reopen the app.
- Confirm the app remains authenticated.
- Wait until the access token expires, then open Profile or save History. The refresh token should renew the access token automatically.

### EV Vehicle Profile

- Select an EV during onboarding.
- Open Profile.
- Confirm the selected EV appears using the EV vehicle card.
- Confirm range and connector text are visible.
- Change EV from Profile and confirm it updates.
- Verify in Neon that `api_userprofile.vehicle_id` changed.

### Map Discovery

- Open Map.
- Confirm stations load from `/api/stations/`.
- Confirm filters work.
- Confirm station details show speed, connectors, rating, image, and availability.

### Routing

- On a real device, grant location permission.
- Tap a station and open its details.
- Tap `Go Charge There`.
- Confirm the route polyline follows roads.
- Confirm ETA and distance appear.
- Confirm a route-only history session is created.

For local debug only:
- If the emulator GPS is missing, routing starts from the debug Tunis test origin.
- This fallback must not appear in release APKs.

### Favorites And History

- Save a station.
- Confirm it appears in Saved/Favorites.
- Start a route with `Go Charge There`.
- Open Favorites > Recent Activity.
- Confirm the route appears as a route-only session.
- Verify in Neon:
  - `api_favorite` has the saved station.
  - `api_historysession` has the route-only session.

### Check-In

- Tap `I'm Charging Here` where check-in is enabled.
- Confirm station becomes Busy.
- Confirm points increase.
- Confirm history session is saved as an actual charging session.
- Verify in Neon:
  - `api_checkin`
  - `api_historysession`
  - `api_userprofile.points`
  - `api_chargingstation.availability`

### Community Features

- Submit a contribution.
- Verify it appears in Neon as pending.
- Approve it in Django Admin.
- Confirm it becomes a visible station.
- Submit an alert.
- Confirm alerts appear on the map.

### Ratings

- Rate a station from details or recent activity.
- Confirm duplicate ratings update the same user/station rating.
- Verify in Neon:
  - `api_stationrating`

## 4. Neon Persistence Audit

These user-facing features should persist in Neon:

| Feature | Backend table/model | Status |
|---|---|---|
| Account and JWT auth | `auth_user` | Persistent |
| Selected EV | `api_userprofile.vehicle` | Persistent |
| Current SoC | `api_userprofile.current_soc` | Persistent after backend update fix |
| Points and badge progress | `api_userprofile.points`, computed `badge` property | Points persistent; badge recalculates from points |
| Favorites | `api_favorite` | Persistent when authenticated |
| Route history | `api_historysession(route_only=True)` | Persistent when authenticated |
| Charging sessions | `api_historysession(route_only=False)`, `api_checkin` | Persistent when authenticated |
| Ratings | `api_stationrating` | Persistent |
| Contributions | `api_contributedstation` | Persistent |
| Alerts | `api_communityalert` | Persistent |
| Station busy/offline state | `api_chargingstation` | Persistent |
| EV catalogue | `api_evvehicle` | Persistent |

If an authenticated endpoint returns `401`, the app should refresh the access token. If refresh also fails, the user must sign in again.

### Points And Levels

Points are stored per user in Neon in `api_userprofile.points`.

The level/badge is computed from points:

| Points | Level |
|---:|---|
| `0-49` | Rookie Eco-Driver |
| `50-149` | Power Charger |
| `150+` | Master Navigator |

Current point events:

| Event | Points |
|---|---:|
| Start charging check-in | `+10` |
| Stop charging / mark available | `+5` |

Because the badge is computed from `points`, there is no separate badge column to become stale. If points are persisted, the user's level is recoverable on every login.

### Clean Storage Rules

- User-private data uses authenticated endpoints and is attached to `request.user`.
- Public discovery data (`stations`, `vehicles`, `alerts`, `route`) is readable without a user session.
- Route-only sessions are stored as `api_historysession.route_only=True`.
- Charging sessions are stored as `api_historysession.route_only=False` and check-in events are stored in `api_checkin`.
- Contributions are stored pending admin approval; approved contributions create a station.
- Station photos use local storage unless R2 variables are set in production.

## 5. Last Two Commit Summary

- `57bca69`: added the core mobile app upgrades: auth flow polish, EV profile handling, map/routing UI, favorites/history/profile upgrades, rating, contribution, alerts, points, and design assets.
- `5d2780d`: added full-stack EV data management, production configuration, EV image/name cleanup, production dependencies, and roadmap updates.

## 6. Release Blockers

Do not ship if any of these fail:

- Release APK cannot log in.
- `/api/stations/` returns `401`.
- `/api/route/` does not return a polyline.
- Profile cannot load `/api/users/me/`.
- Route history does not persist to Neon for authenticated users.
- `.env`, API keys, or keystores are committed.
