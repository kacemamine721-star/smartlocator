# Smart Locator

Smart Locator is a native Android application and Django backend for helping EV drivers discover and navigate to charging stations in Tunisia. The application focuses on a map-first experience, fast station discovery, community contributions, and user-specific data tracking.

## Overview

This project consists of an Android frontend (Java/XML) and a Django REST Framework backend. It provides a modern charging-station locator experience with a floating map UI, station preview sheet, detail pages, saved locations, history, and profile sections. The backend manages geospatial queries, user authentication, and data persistence.

## Features

- **Interactive Map:** EV-specific charging station markers (Available, Busy, Inactive) with community alerts and floating filter chips.
- **Connector-Aware Filtering:** Automatically hides stations that are incompatible with the user's selected vehicle.
- **Charge Session Estimator:** Calculates estimated time to 80% charge based on vehicle specs and station power.
- **Real-Time Check-In:** Users can mark a station as "Busy" when charging, awarding them points. Station automatically reverts to "Available" after the estimated charging time.
- **Community Contributions:** Users can contribute missing stations and report issues (flags stations as broken).
- **Rating System:** Interactive star rating and reviews for stations, preventing duplicate ratings by updating existing ones.
- **User Accounts (JWT Auth):** Welcome screen, login, and registration for personalized experiences.

## Tech Stack

**Frontend (Android):**
- Java, XML layouts, Material Components
- Retrofit & OkHttp (API calls)
- Room Database (Local cache)
- MapLibre Android SDK & OpenStreetMap

**Backend (Django):**
- Django & Django REST Framework (DRF)
- `djangorestframework-simplejwt` (Authentication)
- SQLite (Local Development)
- **Production Stack (Planned):**
  - **PostgreSQL + PostGIS:** Hosted on **Neon** (Permanent free tier with connection pooling).
  - **Redis Cache:** Hosted on **Upstash** (Serverless, used for EV catalogue and viewport caching).
  - **File Storage:** **Cloudflare R2** (S3-compatible, zero egress fees for images).
  - **Hosting:** **Render.com** (Web Service).

## Project Structure

```text
smartlocator/
├── app/ (Android Frontend)
│   ├── src/main/java/com/example/project_mobile/
│   │   ├── MainActivity.java
│   │   ├── data/ (Room DB, Repositories, TokenManager, RetrofitClient)
│   │   └── ui/ (Map, Details, Favorites, History, Profile, Auth, Contribution)
├── backend/ (Django Backend)
│   ├── core/ (Settings for Local & Production)
│   ├── api/ (Models, Views, Serializers for Stations, Auth, etc.)
│   └── db.sqlite3 (Local development database)
└── gradle.properties (Build configurations)
```

## Requirements

- Android Studio (Android SDK 36, API 24+)
- JDK 21 (Required for Android builds)
- Python 3.9+ (For backend development)

## Getting Started

### Backend Setup
1. Navigate to the `backend` directory.
2. Install dependencies: `pip install -r requirements.txt`
3. Run migrations: `python manage.py migrate`
4. Start the development server: `python manage.py runserver 0.0.0.0:8000`
5. Enter the admin interface: `http://127.0.0.1:8000/admin/`

### Android Setup
1. Open the root project folder in Android Studio.
2. The app is pre-configured to connect to the local backend emulator address (`http://10.0.2.2:8000/api/`).
3. Sync Gradle dependencies. *(Note: `org.gradle.java.home` is set to JDK 21 in `gradle.properties` to fix compilation issues)*.
4. Run the app on an Android emulator.

## Roadmap & Enhancements

- [x] **Welcome & Auth:** Finalize Welcome screen, Login/Register UI, and robust JWT token handling.
- [x] **Map UI Polish:** Implement custom EV markers (Green/Red/Grey) and community alerts.
- [x] **Contributions & Personal Data:** Link the "Contribute a Station" form to the backend and securely sync user favorites/history.
- [x] **Vehicle & Charging Simulation:** Added EV database, image matching, and automatic station availability reset based on vehicle specs.
- [ ] **Station Photos:** Implement upload endpoint for station images and display them in the app.
- [ ] **Production Deployment:** Migrate the backend to Neon (PostgreSQL), Upstash (Redis), and Cloudflare R2 (Storage) and deploy to Render.com.

## License

This project is currently provided for academic and demonstration purposes.
