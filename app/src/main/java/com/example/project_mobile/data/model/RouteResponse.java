package com.example.project_mobile.data.model;

import com.google.gson.annotations.SerializedName;

public class RouteResponse {
    @SerializedName("distance_m")
    public double distanceM;

    @SerializedName("duration_s")
    public long durationS;

    @SerializedName("polyline")
    public String polyline;
}
