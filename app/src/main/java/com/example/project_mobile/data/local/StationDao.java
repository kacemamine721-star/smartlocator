package com.example.project_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StationDao {

    @Query("SELECT * FROM stations ORDER BY name ASC")
    LiveData<List<StationEntity>> getAll();

    @Query("SELECT * FROM stations WHERE id = :id LIMIT 1")
    StationEntity getById(int id);

    @Query("SELECT COUNT(*) FROM stations")
    int count();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StationEntity> stations);

    @Query("UPDATE stations SET isFavorite = 1 WHERE id = :stationId")
    void markFavorite(int stationId);

    @Query("UPDATE stations SET isFavorite = :isFav WHERE id = :stationId")
    void setFavorite(int stationId, boolean isFav);
    
    @Query("UPDATE stations SET status = :status WHERE id = :stationId")
    void updateStatus(int stationId, String status);

    @Query("UPDATE stations SET isFavorite = 0 WHERE id = :stationId")
    void unmarkFavorite(int stationId);

    @Query("SELECT * FROM stations WHERE isFavorite = 1")
    LiveData<List<StationEntity>> getFavorites();

    @Query("SELECT s.* FROM stations s INNER JOIN favorites f ON s.id = f.stationId WHERE f.userId = :userId")
    LiveData<List<StationEntity>> getFavoritesForUser(String userId);

    @Query("SELECT * FROM stations WHERE csSpeed LIKE :speedFilter")
    LiveData<List<StationEntity>> getBySpeed(String speedFilter);
}
