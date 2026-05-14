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
import com.example.project_mobile.ui.common.StationListAdapter;

public class FavoritesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    private FavoritesViewModel viewModel;
    private StationListAdapter adapter;
    private java.util.List<ChargingStation> fullList;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(FavoritesViewModel.class);
        
        RecyclerView recyclerView = view.findViewById(R.id.favorites_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initialize adapter with empty list
        adapter = new StationListAdapter(new java.util.ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Observe favorites from DB
        viewModel.getFavoriteStations().observe(getViewLifecycleOwner(), stations -> {
            this.fullList = stations;
            performFiltering(""); // Initial load
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
                viewModel.removeFavorite(Integer.parseInt(station.id), null);
            }
        }).attachToRecyclerView(recyclerView);
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
