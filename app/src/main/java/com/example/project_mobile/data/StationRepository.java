package com.example.project_mobile.data;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.project_mobile.data.local.AppDatabase;
import com.example.project_mobile.data.local.ContributionDao;
import com.example.project_mobile.data.local.ContributionEntity;
import com.example.project_mobile.data.local.FavoriteDao;
import com.example.project_mobile.data.local.FavoriteEntity;
import com.example.project_mobile.data.local.GeoJsonLoader;
import com.example.project_mobile.data.local.HistoryDao;
import com.example.project_mobile.data.local.HistoryEntity;
import com.example.project_mobile.data.local.StationDao;
import com.example.project_mobile.data.local.StationEntity;
import com.example.project_mobile.data.remote.ApiService;
import com.example.project_mobile.data.remote.ContributionRequest;
import com.example.project_mobile.data.remote.ContributionResponse;
import com.example.project_mobile.data.remote.RetrofitClient;
import com.example.project_mobile.data.remote.StationDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;

/**
 * Single source of truth for station data.
 * Used by all teammates' ViewModels.
 * Offline-first: Room is the primary data source.
 * Network sync will be added by Student 4 (ApiClient / Retrofit).
 */
public class StationRepository {

    private static final String TAG = "StationRepository";

    private final StationDao stationDao;
    private final FavoriteDao favoriteDao;
    private final HistoryDao historyDao;
    private final ContributionDao contributionDao;
    private final TokenManager tokenManager;
    private final Application app;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface Callback {
        void onSuccess();

        void onError(String message);
    }

    public StationRepository(Application app) {
        this.app = app;
        AppDatabase db = AppDatabase.getInstance(app);
        stationDao = db.stationDao();
        favoriteDao = db.favoriteDao();
        historyDao = db.historyDao();
        contributionDao = db.contributionDao();
        tokenManager = new TokenManager(app);
        executor = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());

        // Seed DB from GeoJSON on first launch (background thread)
        executor.execute(() -> {
            GeoJsonLoader.seedIfEmpty(app, stationDao);
            syncWithBackend(); // Sync with API immediately after seeding
        });
    }

    /**
     * Fetches stations from the Django backend and updates the local database.
     */
    public void syncWithBackend() {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Syncing with backend...");
                ApiService api = RetrofitClient.getApiService(app);
                Response<List<StationDto>> response = api.getStations().execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<StationEntity> entities = new ArrayList<>();
                    for (StationDto dto : response.body()) {
                        StationEntity e = new StationEntity();
                        e.id = Integer.parseInt(dto.stationId);
                        e.name = dto.name;
                        e.address = dto.address;
                        e.city = dto.city;
                        e.power = dto.power;
                        e.network = dto.network;
                        e.reliability = dto.reliability;
                        e.status = dto.availability;
                        e.latitude = dto.latitude;
                        e.longitude = dto.longitude;
                        
                        // IMPORTANT: Preserve local favorite status. 
                        // The backend 'is_favorite' is global/featured, not per-user.
                        StationEntity existing = stationDao.getById(e.id);
                        if (existing != null) {
                            e.isFavorite = existing.isFavorite;
                        } else {
                            e.isFavorite = dto.isFavorite;
                        }
                        
                        entities.add(e);
                    }
                    stationDao.insertAll(entities);
                    Log.d(TAG, "Successfully synced " + entities.size() + " stations from backend.");
                } else {
                    Log.e(TAG, "Backend sync failed: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing with backend: " + e.getMessage());
            }
        });
    }

    /** All stations as LiveData — auto-updates when DB changes */
    public LiveData<List<ChargingStation>> getAllStations() {
        return Transformations.map(stationDao.getAll(), this::mapEntitiesToModel);
    }

    /** Stations filtered by speed — used by Student 2 filter chips */
    public LiveData<List<ChargingStation>> getStationsBySpeed(String speedFilter) {
        return Transformations.map(stationDao.getBySpeed("%" + speedFilter + "%"),
                this::mapEntitiesToModel);
    }

    public LiveData<List<ChargingStation>> getFavorites() {
        return Transformations.map(stationDao.getFavoritesForUser(tokenManager.getUserId()), this::mapEntitiesToModel);
    }

    public LiveData<List<Integer>> getFavoriteIds() {
        return favoriteDao.getFavoriteIds(tokenManager.getUserId());
    }

    public void addFavorite(int stationId, Callback callback) {
        executor.execute(() -> {
            try {
                String uid = tokenManager.getUserId();
                if (favoriteDao.isFavorite(stationId, uid)) {
                    postSuccess(callback);
                    return;
                }
                FavoriteEntity fav = new FavoriteEntity();
                fav.stationId = stationId;
                fav.userId = uid;
                fav.savedAt = System.currentTimeMillis();
                favoriteDao.insert(fav);
                stationDao.markFavorite(stationId);
                postSuccess(callback);
            } catch (Exception e) {
                postError(callback, e.getMessage());
            }
        });
    }

    public void removeFavorite(int stationId, Callback callback) {
        executor.execute(() -> {
            try {
                favoriteDao.remove(stationId, tokenManager.getUserId());
                stationDao.unmarkFavorite(stationId);
                postSuccess(callback);
            } catch (Exception e) {
                postError(callback, e.getMessage());
            }
        });
    }

    public LiveData<List<HistoryEntity>> getHistory() {
        return historyDao.getForUser(tokenManager.getUserId());
    }

    /**
     * Called by Student 3 (RoutingViewModel) when a route is started or a charge is
     * logged.
     * 
     * @param stationId   station being visited
     * @param stationName station display name
     * @param routeOnly   true = navigation only, false = actual charging session
     * @param kwh         kWh charged (0 if routeOnly)
     * @param durationMin session duration in minutes (0 if unknown)
     */
    public void saveSession(int stationId, String stationName, String city,
            boolean routeOnly, float kwh, int durationMin) {
        executor.execute(() -> {
            HistoryEntity h = new HistoryEntity();
            h.stationId = stationId;
            h.stationName = stationName;
            h.city = city;
            h.date = System.currentTimeMillis();
            h.routeOnly = routeOnly;
            h.kwhCharged = kwh;
            h.durationMin = durationMin;
            h.userId = tokenManager.getUserId();
            historyDao.insert(h);
            Log.d(TAG, "Session saved for station " + stationId);
        });
    }

    public void submitRating(int stationId, int stars, String comment, Callback callback) {
        executor.execute(() -> {
            try {
                // TODO (Student 4): also POST to /api/stations/{id}/rate/
                Log.d(TAG, "Rating submitted: " + stars + "★ for station " + stationId);
                postSuccess(callback);
            } catch (Exception e) {
                postError(callback, e.getMessage());
            }
        });
    }

    public void submitContribution(double lat, double lng, String name,
            String csSpeed, String status, Callback callback) {
        executor.execute(() -> {
            try {
                ContributionEntity c = new ContributionEntity();
                c.latitude = lat;
                c.longitude = lng;
                c.name = name;
                c.csSpeed = csSpeed;
                c.status = status;
                c.userId = tokenManager.getUserId();
                c.submittedAt = System.currentTimeMillis();
                c.approved = false;
                ApiService api = RetrofitClient.getApiService(app);
                Response<ContributionResponse> response = api.submitContribution(
                        new ContributionRequest(name, lat, lng, csSpeed, status)
                ).execute();
                if (response.isSuccessful() && response.body() != null) {
                    c.remoteId = String.valueOf(response.body().id);
                } else {
                    postError(callback, "Backend rejected contribution: " + response.code());
                    return;
                }
                contributionDao.insert(c);
                postSuccess(callback);
            } catch (Exception e) {
                postError(callback, e.getMessage());
            }
        });
    }

    public List<ContributionEntity> getMyContributions() {
        return contributionDao.getForUser(tokenManager.getUserId());
    }

    // ─────────────────────────────────────────────
    // PROFILE STATS
    // ─────────────────────────────────────────────

    public MutableLiveData<int[]> getProfileStats() {
        MutableLiveData<int[]> stats = new MutableLiveData<>();
        executor.execute(() -> {
            String uid = tokenManager.getUserId();
            int saved = favoriteDao.countForUser(uid);
            int routes = historyDao.countRoutes(uid);
            mainHandler.post(() -> stats.setValue(new int[] { saved, routes, 0 }));
        });
        return stats;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private List<ChargingStation> mapEntitiesToModel(List<StationEntity> entities) {
        List<ChargingStation> result = new ArrayList<>();
        if (entities == null)
            return result;
        for (StationEntity e : entities) {
            result.add(new ChargingStation(
                    String.valueOf(e.id),
                    e.name,
                    e.address,
                    e.city,
                    "", // distance — computed from GPS
                    "", // eta
                    e.status,
                    e.power, // Use power for speed if applicable
                    "", // ports
                    "", // hours
                    e.network,
                    "", // price
                    e.reliability,
                    e.isFavorite,
                    e.latitude,
                    e.longitude,
                    new ArrayList<>(),
                    e.averageRating,
                    e.ratingCount));
        }
        return result;
    }

    private void postSuccess(Callback cb) {
        if (cb != null)
            mainHandler.post(cb::onSuccess);
    }

    private void postError(Callback cb, String msg) {
        if (cb != null)
            mainHandler.post(() -> cb.onError(msg));
    }
}
