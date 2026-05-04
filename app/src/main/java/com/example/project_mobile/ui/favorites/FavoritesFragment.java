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
import com.example.project_mobile.data.MockStationRepository;
import com.example.project_mobile.ui.common.StationListAdapter;

public class FavoritesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    private FavoritesViewModel viewModel;
    private StationListAdapter adapter;

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
            adapter.updateStations(stations);
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
}
