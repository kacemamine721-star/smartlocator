package com.example.project_mobile.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stations")
public class StationEntity {

    @PrimaryKey
    public int id;
    public String name;
    public String address;
    public String city;
    public String power;
    public String network;
    public String reliability;
    public String status;       // AVAILABLE | BUSY | OFFLINE | UNKNOWN
    public String csSpeed;      // FAST | SEMI-FAST | SLOW
    public String origin;       // electromaps | google maps | user
    public String reportUs;
    public double latitude;
    public double longitude;
    public boolean isFavorite;
    public float averageRating;
    public int ratingCount;
    public boolean isUserContributed;
}
