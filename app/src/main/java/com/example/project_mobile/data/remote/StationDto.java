package com.example.project_mobile.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StationDto {
    @SerializedName("station_id")
    public String stationId;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("address")
    public String address;
    
    @SerializedName("city")
    public String city;
    
    @SerializedName("availability")
    public String availability;
    
    @SerializedName("power")
    public String power;
    
    @SerializedName("network")
    public String network;
    
    @SerializedName("reliability")
    public String reliability;
    
    @SerializedName("latitude")
    public double latitude;
    
    @SerializedName("longitude")
    public double longitude;
    
    @SerializedName("is_favorite")
    public boolean isFavorite;

    @SerializedName("connectors")
    public List<String> connectors;
}
