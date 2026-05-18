package com.example.project_mobile.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.R;
import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.data.StationRepository;
import com.example.project_mobile.ui.common.StationListAdapter;
import com.example.project_mobile.ui.history.HistoryAdapter;
import com.example.project_mobile.ui.history.HistoryViewModel;

public class FavoritesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    private FavoritesViewModel viewModel;
    private HistoryViewModel historyViewModel;
    private StationListAdapter adapter;
    private java.util.List<ChargingStation> fullList;
    private java.util.List<ChargingStation> favoriteList = new java.util.ArrayList<>();
    private java.util.List<ChargingStation> allStationList = new java.util.ArrayList<>();
    private android.widget.TextView stationSectionTitle;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(FavoritesViewModel.class);
        historyViewModel = new androidx.lifecycle.ViewModelProvider(this).get(HistoryViewModel.class);
        StationRepository repository = new StationRepository(requireActivity().getApplication());
        
        RecyclerView recyclerView = view.findViewById(R.id.favorites_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initialize adapter with empty list
        adapter = new StationListAdapter(new java.util.ArrayList<>());
        recyclerView.setAdapter(adapter);
        android.widget.TextView btnStations = view.findViewById(R.id.btn_stations);
        android.widget.TextView btnRecentActivity = view.findViewById(R.id.btn_recent_activity);
        stationSectionTitle = btnStations;

        RecyclerView historyRecycler = view.findViewById(R.id.favorites_history_list);
        historyRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        HistoryAdapter historyAdapter = new HistoryAdapter(session ->
                com.example.project_mobile.ui.rating.RatingBottomSheet.newInstance(
                                session.stationId, session.stationName)
                        .show(requireActivity().getSupportFragmentManager(), "rating_sheet"));
        historyRecycler.setAdapter(historyAdapter);

        btnStations.setOnClickListener(v -> {
            recyclerView.setVisibility(View.VISIBLE);
            historyRecycler.setVisibility(View.GONE);
            btnStations.setTextColor(getResources().getColor(R.color.zid_text_primary));
            btnRecentActivity.setTextColor(getResources().getColor(R.color.zid_text_secondary));
            btnStations.setAlpha(1.0f);
            btnRecentActivity.setAlpha(0.6f);
        });

        btnRecentActivity.setOnClickListener(v -> {
            recyclerView.setVisibility(View.GONE);
            historyRecycler.setVisibility(View.VISIBLE);
            btnStations.setTextColor(getResources().getColor(R.color.zid_text_secondary));
            btnRecentActivity.setTextColor(getResources().getColor(R.color.zid_text_primary));
            btnStations.setAlpha(0.6f);
            btnRecentActivity.setAlpha(1.0f);
        });
        historyViewModel.getHistorySessions().observe(getViewLifecycleOwner(), historyAdapter::updateSessions);

        // Observe favorites from DB
        viewModel.getFavoriteStations().observe(getViewLifecycleOwner(), stations -> {
            this.favoriteList = stations == null ? new java.util.ArrayList<>() : stations;
            chooseVisibleStationSource();
            performFiltering(currentSearch()); // Initial load
        });
        repository.getAllStations().observe(getViewLifecycleOwner(), stations -> {
            this.allStationList = stations == null ? new java.util.ArrayList<>() : stations;
            chooseVisibleStationSource();
            performFiltering(currentSearch());
        });

        android.widget.EditText searchInput = view.findViewById(R.id.et_search_favorites);
        if (searchInput != null) {
            searchInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(android.text.Editable s) {
                    performFiltering(s.toString());
                }
            });
        }

        android.widget.TextView tvFast = view.findViewById(R.id.tv_fast_chargers_count);
        android.widget.TextView tvTrusted = view.findViewById(R.id.tv_trusted_stops_count);

        viewModel.getFastChargerCount().observe(getViewLifecycleOwner(), count -> {
            tvFast.setText(count + " stations");
        });

        viewModel.getTrustedStopsCount().observe(getViewLifecycleOwner(), count -> {
            tvTrusted.setText(count + " stations");
        });

        // Swipe to delete
        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ChargingStation station = adapter.getStationAt(position);
                if (station.favorite) {
                    viewModel.removeFavorite(Integer.parseInt(station.id), null);
                } else {
                    adapter.notifyItemChanged(position);
                    android.widget.Toast.makeText(requireContext(), "Save a station first, then swipe to remove it.", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private String currentSearch() {
        if (getView() == null) return "";
        android.widget.EditText input = getView().findViewById(R.id.et_search_favorites);
        return input != null && input.getText() != null ? input.getText().toString() : "";
    }

    private void chooseVisibleStationSource() {
        boolean hasFavorites = favoriteList != null && !favoriteList.isEmpty();
        fullList = hasFavorites ? favoriteList : allStationList;
        if (stationSectionTitle != null) {
            stationSectionTitle.setText(hasFavorites ? getString(R.string.recently_saved) : "Stations");
        }
    }

    private void performFiltering(String query) {
        if (fullList == null) return;
        if (query == null || query.isEmpty()) {
            adapter.updateStations(fullList);
        } else {
            String q = query.toLowerCase(java.util.Locale.US);
            java.util.List<ChargingStation> filtered = new java.util.ArrayList<>();
            for (ChargingStation s : fullList) {
                if ((s.name != null && s.name.toLowerCase(java.util.Locale.US).contains(q)) ||
                    (s.city != null && s.city.toLowerCase(java.util.Locale.US).contains(q))) {
                    filtered.add(s);
                }
            }
            adapter.updateStations(filtered);
        }
    }
}
