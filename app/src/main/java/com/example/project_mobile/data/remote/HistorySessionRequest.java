package com.example.project_mobile.data.remote;

import com.google.gson.annotations.SerializedName;

public class HistorySessionRequest {
    public final int station;

    @SerializedName("route_only")
    public final boolean routeOnly;

    @SerializedName("kwh_charged")
    public final float kwhCharged;

    @SerializedName("duration_min")
    public final int durationMin;

    public HistorySessionRequest(int station, boolean routeOnly, float kwhCharged, int durationMin) {
        this.station = station;
        this.routeOnly = routeOnly;
        this.kwhCharged = kwhCharged;
        this.durationMin = durationMin;
    }
}
