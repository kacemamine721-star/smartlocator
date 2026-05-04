package com.example.project_mobile.ui.profile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.project_mobile.data.StationRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final StationRepository repository;
    private final LiveData<int[]> profileStats;

    public ProfileViewModel(Application application) {
        super(application);
        repository = new StationRepository(application);
        profileStats = repository.getProfileStats();
    }

    public LiveData<int[]> getProfileStats() {
        return profileStats;
    }

    public String getUserName() {
        return repository.getTokenManager().getUserName();
    }
    
    public void signOut() {
        repository.getTokenManager().clear();
    }
}
