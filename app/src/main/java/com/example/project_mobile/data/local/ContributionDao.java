package com.example.project_mobile.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ContributionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContributionEntity contribution);

    @Query("SELECT * FROM contributions WHERE userId = :userId ORDER BY submittedAt DESC")
    List<ContributionEntity> getForUser(String userId);

    @Query("UPDATE contributions SET approved = 1 WHERE remoteId = :remoteId")
    void markApproved(String remoteId);
}
