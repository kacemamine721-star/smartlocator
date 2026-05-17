package com.example.project_mobile.data.remote;

public class RatingRequest {
    public final int station;
    public final int stars;
    public final String comment;

    public RatingRequest(int station, int stars, String comment) {
        this.station = station;
        this.stars = stars;
        this.comment = comment;
    }
}
