package com.example.project_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryEntity session);

    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<HistoryEntity>> getForUser(String userId);

    @Query("SELECT COUNT(*) FROM history WHERE userId = :userId AND routeOnly = 0")
    int countChargingSessions(String userId);

    @Query("SELECT COUNT(*) FROM history WHERE userId = :userId")
    int countRoutes(String userId);

    @Query("DELETE FROM history WHERE userId = :userId")
    void clearForUser(String userId);
}
