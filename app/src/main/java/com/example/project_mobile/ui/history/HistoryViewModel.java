package com.example.project_mobile.ui.history;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.project_mobile.data.StationRepository;
import com.example.project_mobile.data.local.HistoryEntity;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final StationRepository repository;
    private final LiveData<List<HistoryEntity>> historySessions;

    public HistoryViewModel(Application application) {
        super(application);
        repository = new StationRepository(application);
        historySessions = repository.getHistory();
    }

    public LiveData<List<HistoryEntity>> getHistorySessions() {
        return historySessions;
    }
}
