# Smart Locator

Smart Locator is a native Android application and Django backend for helping EV drivers discover and navigate to charging stations in Tunisia. The application focuses on a map-first experience, fast station discovery, community contributions, and user-specific data tracking.

## Overview

This project consists of an Android frontend (Java/XML) and a Django REST Framework backend. It provides a modern charging-station locator experience with a floating map UI, station preview sheet, detail pages, saved locations, history, and profile sections. The backend manages geospatial queries, user authentication, and data persistence.

## Features

- **Interactive Map:** EV-specific charging station markers (Available, Busy, Inactive) with simulated alerts and floating filter chips.
- **Station Previews & Details:** Bottom sheet preview with availability, power, ports, and connectors.
- **User Accounts (JWT Auth):** Welcome screen, login, and registration for personalized experiences.
- **Personalized Data:** Favorites screen for saved stations and History screen for recent activity.
- **Profile & Settings:** User summary, preferences, and a working Sign Out flow.
- **Community Contributions:** Users can contribute missing stations directly from the app.
- **Free Map Stack:** Uses MapLibre and OpenStreetMap raster tiles.

## Tech Stack

**Frontend (Android):**
- Java, XML layouts, Material Components
- Retrofit & OkHttp (API calls)
- Room Database (Local cache)
- MapLibre Android SDK & OpenStreetMap

**Backend (Django):**
- Django & Django REST Framework (DRF)
- `djangorestframework-simplejwt` (Authentication)
- GeoDjango, PostgreSQL + PostGIS (Production), SQLite (Local Development)

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

### Android Setup
1. Open the root project folder in Android Studio.
2. The app is pre-configured to connect to the local backend emulator address (`http://10.0.2.2:8000/api/`).
3. Sync Gradle dependencies. *(Note: `org.gradle.java.home` is set to JDK 21 in `gradle.properties` to fix compilation issues)*.
4. Run the app on an Android emulator.

## Roadmap & Enhancements

- **Welcome & Auth:** Finalize Welcome screen, Login/Register UI, and robust JWT token handling.
- **Map UI Polish:** Implement custom EV markers (Green/Red/Grey) and a simulated Alert banner.
- **Contributions & Personal Data:** Link the "Contribute a Station" form to the backend and securely sync user favorites/history via their specific account.
- **Production Deployment:** Migrate the backend from local SQLite to PostgreSQL + PostGIS and deploy to Render.com.

## License

This project is currently provided for academic and demonstration purposes.
