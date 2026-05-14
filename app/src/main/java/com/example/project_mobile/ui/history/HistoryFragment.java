package com.example.project_mobile.ui.history;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryFragment extends Fragment {

    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(HistoryViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.history_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new HistoryAdapter(session -> {
            com.example.project_mobile.ui.rating.RatingBottomSheet.newInstance(
                    session.stationId, session.stationName)
                .show(requireActivity().getSupportFragmentManager(), "rating_sheet");
        });
        recyclerView.setAdapter(adapter);

        viewModel.getHistorySessions().observe(getViewLifecycleOwner(), sessions -> {
            adapter.updateSessions(sessions);
        });
    }
}
