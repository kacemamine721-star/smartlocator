package com.example.project_mobile.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database singleton — shared by all teammates.
 * Access via AppDatabase.getInstance(context).stationDao() etc.
 */
@Database(
        entities = {
                StationEntity.class,
                FavoriteEntity.class,
                HistoryEntity.class,
                RatingEntity.class,
                ContributionEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract StationDao stationDao();
    public abstract FavoriteDao favoriteDao();
    public abstract HistoryDao historyDao();
    public abstract ContributionDao contributionDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "smartlocator.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
