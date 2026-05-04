package com.example.project_mobile.ui.favorites;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.data.StationRepository;

import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {

    private final StationRepository repository;
    private final LiveData<List<ChargingStation>> favoriteStations;

    public FavoritesViewModel(Application application) {
        super(application);
        repository = new StationRepository(application);
        favoriteStations = repository.getFavorites();
    }

    public LiveData<List<ChargingStation>> getFavoriteStations() {
        return favoriteStations;
    }

    public void removeFavorite(int stationId, StationRepository.Callback callback) {
        repository.removeFavorite(stationId, callback);
    }
}
