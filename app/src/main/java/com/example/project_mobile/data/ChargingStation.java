package com.example.project_mobile.data;

import java.util.ArrayList;
import java.util.List;

public class ChargingStation {

    public final String id;
    public final String name;
    public final String address;
    public final String city;
    public final String distance;
    public final String eta;
    public final String status;
    public final String power;
    public final String ports;
    public final String hours;
    public final String provider;
    public final String price;
    public final String reliability;
    public final boolean favorite;
    public final double latitude;
    public final double longitude;
    public final List<String> connectors;

    public ChargingStation(
            String id,
            String name,
            String address,
            String city,
            String distance,
            String eta,
            String status,
            String power,
            String ports,
            String hours,
            String provider,
            String price,
            String reliability,
            boolean favorite,
            double latitude,
            double longitude,
            List<String> connectors
    ) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.distance = distance;
        this.eta = eta;
        this.status = status;
        this.power = power;
        this.ports = ports;
        this.hours = hours;
        this.provider = provider;
        this.price = price;
        this.reliability = reliability;
        this.favorite = favorite;
        this.latitude = latitude;
        this.longitude = longitude;
        this.connectors = new ArrayList<>(connectors);
    }
}
