package com.example.project_mobile.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ratings")
public class RatingEntity {

    @PrimaryKey(autoGenerate = true)
    public int localId;
    public int stationId;
    public String userId;
    public int stars;           // 1–5
    public String comment;
    public long ratedAt;
}
