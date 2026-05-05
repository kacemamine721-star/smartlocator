package com.example.project_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(FavoriteEntity favorite);

    @Query("DELETE FROM favorites WHERE stationId = :stationId AND userId = :userId")
    void remove(int stationId, String userId);

    @Query("SELECT stationId FROM favorites WHERE userId = :userId")
    LiveData<List<Integer>> getFavoriteIds(String userId);

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    int countForUser(String userId);

    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE stationId = :stationId AND userId = :userId")
    boolean isFavorite(int stationId, String userId);
}
