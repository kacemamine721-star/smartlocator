package com.example.project_mobile.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class HistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int localId;
    public int stationId;
    public String stationName;
    public String city;
    public long date;           // System.currentTimeMillis()
    public float kwhCharged;    // 0 if routeOnly
    public boolean routeOnly;
    public int durationMin;
    public String userId;
}
