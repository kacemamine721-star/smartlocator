package com.example.project_mobile.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contributions")
public class ContributionEntity {

    @PrimaryKey(autoGenerate = true)
    public int localId;
    public String remoteId;     // UUID returned by the backend
    public String userId;
    public String name;
    public double latitude;
    public double longitude;
    public String csSpeed;
    public String status;
    public long submittedAt;
    public boolean approved;
}
