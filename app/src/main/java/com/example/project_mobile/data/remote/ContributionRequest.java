package com.example.project_mobile.data.remote;

public class ContributionRequest {
    public final String name;
    public final double latitude;
    public final double longitude;
    public final String speed;
    public final String status;

    public ContributionRequest(String name, double latitude, double longitude, String speed, String status) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.status = status;
    }
}
