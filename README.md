# Smart Locator

Smart Locator is a native Android prototype for helping EV drivers discover and navigate to charging stations in Tunisia. The application focuses on a map-first experience, fast station discovery, and a clean mobile interface designed for practical on-the-go use.

## Overview

This project was built as an Android application using Java and XML. It provides a modern charging-station locator experience with a floating map UI, station preview sheet, detail pages, saved locations, history, and profile sections.

The current version is a prototype powered by mock station data and OpenStreetMap-based map rendering through MapLibre.

## Features

- Interactive map with EV charging station markers
- Floating filter chips for quick station discovery
- Bottom station preview sheet with availability, power, ports, and connectors
- Station details screen with operational information and quick actions
- Favorites screen for saved stations and curated collections
- History screen for recent activity
- Profile screen with driver stats and settings sections
- Alerts screen entry point for charging-related notifications
- Free map stack using MapLibre and OpenStreetMap

## Tech Stack

- Android Studio
- Java
- XML layouts
- Material Components
- RecyclerView
- MapLibre Android SDK
- OpenStreetMap raster tiles

## Project Structure

```text
app/src/main/java/com/example/project_mobile/
├── MainActivity.java
├── data/
│   ├── ChargingStation.java
│   └── MockStationRepository.java
└── ui/
    ├── alerts/
    ├── common/
    ├── details/
    ├── favorites/
    ├── history/
    ├── map/
    └── profile/
```

## Screens

- `Map`: main discovery experience with map, markers, filters, and station preview
- `Station Details`: detailed information for a selected charging station
- `Favorites`: saved stations and curated collections
- `History`: recent routing and charging activity
- `Profile`: user summary, preferences, and settings

## Requirements

- Android Studio
- Android SDK 36
- Minimum Android version: API 24
- JDK 11

## Getting Started

1. Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/smartlocator.git
cd smartlocator
```

2. Open the project in Android Studio.

3. Sync Gradle dependencies.

4. Run the app on an emulator or Android device.

## Build

To build the debug APK from the command line:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Notes

- The current application uses mock charging-station data for demonstration and UI validation.
- The project intentionally uses a free map stack instead of Google Maps.
- Local files such as `local.properties`, Gradle caches, build outputs, and debug keystores are excluded from version control through `.gitignore`.

## Roadmap

- Add real charging-station data sources for Tunisia
- Turn visual filter chips into live filtering logic
- Add station search functionality
- Improve route planning and charging-stop recommendations
- Add persistent favorites and history storage
- Introduce user authentication and sync

## License

This project is currently provided for academic and demonstration purposes.
