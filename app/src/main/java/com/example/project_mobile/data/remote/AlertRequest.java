package com.example.project_mobile.data.remote;

public class AlertRequest {
    public String alert_type;
    public String description;
    public double latitude;
    public double longitude;

    public AlertRequest(String alert_type, String description, double latitude, double longitude) {
        this.alert_type = alert_type;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
