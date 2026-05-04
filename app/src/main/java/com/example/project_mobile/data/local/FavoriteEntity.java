package com.example.project_mobile.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteEntity {

    @PrimaryKey(autoGenerate = true)
    public int localId;
    public int stationId;
    public String userId;       // JWT user id from backend
    public long savedAt;        // System.currentTimeMillis()
}
