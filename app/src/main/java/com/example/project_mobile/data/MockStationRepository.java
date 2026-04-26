package com.example.project_mobile.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MockStationRepository {

    private MockStationRepository() {
    }

    public static List<ChargingStation> getStations() {
        List<ChargingStation> stations = new ArrayList<>();
        stations.add(new ChargingStation(
                "lac-2",
                "Lac 2 Fast Hub",
                "Avenue de la Bourse, Les Berges du Lac 2",
                "Tunis",
                "2.1 km",
                "6 min",
                "Available",
                "150 kW",
                "6 ports",
                "Open 24/7",
                "ChargeTN",
                "0.95 TND/kWh",
                "Reliable 96%",
                true,
                36.8489,
                10.2723,
                Arrays.asList("CCS2", "Type 2", "DC Fast")
        ));
        stations.add(new ChargingStation(
                "marsa",
                "La Marsa Corniche",
                "Corniche Road, La Marsa",
                "Tunis",
                "8.4 km",
                "18 min",
                "Busy",
                "80 kW",
                "4 ports",
                "06:00 - 23:00",
                "VoltGo",
                "0.88 TND/kWh",
                "Reliable 89%",
                true,
                36.8863,
                10.3248,
                Arrays.asList("Type 2", "AC", "CCS2")
        ));
        stations.add(new ChargingStation(
                "sousse",
                "Sousse Downtown Station",
                "Boulevard du 14 Janvier, Sousse",
                "Sousse",
                "143 km",
                "1h 42m",
                "Offline",
                "60 kW",
                "3 ports",
                "Open 24/7",
                "GreenGrid",
                "0.79 TND/kWh",
                "Reliable 73%",
                false,
                35.8256,
                10.6369,
                Arrays.asList("CCS2", "CHAdeMO", "Type 2")
        ));
        return stations;
    }

    public static List<ChargingStation> getFavorites() {
        List<ChargingStation> favorites = new ArrayList<>();
        for (ChargingStation station : getStations()) {
            if (station.favorite) {
                favorites.add(station);
            }
        }
        return favorites;
    }

    public static List<String> getHistory() {
        return Arrays.asList(
                "Today - Lac 2 Fast Hub - Tunis - 21 kWh",
                "Yesterday - La Marsa Corniche - Tunis - 14 kWh",
                "Apr 22 - Hammamet South Stop - Nabeul - 18 kWh",
                "Apr 19 - Sousse Downtown Station - Sousse - Route planning only"
        );
    }

    public static List<String> getAlerts() {
        return Arrays.asList(
                "Lac 2 Fast Hub: 2 ports became available",
                "La Marsa Corniche is experiencing high demand",
                "Sousse Downtown Station is currently offline"
        );
    }
}
