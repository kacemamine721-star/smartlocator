package com.example.project_mobile.data.remote;

import com.google.gson.annotations.SerializedName;

public class CommunityAlert {
    @SerializedName("id")
    public int id;

    @SerializedName("alert_type")
    public String alertType;

    @SerializedName("description")
    public String description;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("is_active")
    public boolean isActive;

    @SerializedName("is_validated")
    public boolean isValidated;

    @SerializedName("created_at")
    public String createdAt;
}
