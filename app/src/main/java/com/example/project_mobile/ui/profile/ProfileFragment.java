package com.example.project_mobile.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;

import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(ProfileViewModel.class);

        TextView initialsText = view.findViewById(R.id.profile_initials);
        TextView nameText = view.findViewById(R.id.profile_name);
        TextView savedCount = view.findViewById(R.id.profile_stat_saved_count);
        TextView routesCount = view.findViewById(R.id.profile_stat_routes_count);
        TextView alertsCount = view.findViewById(R.id.profile_stat_alerts_count);

        String userName = viewModel.getUserName();
        nameText.setText(userName);
        if (userName != null && !userName.isEmpty()) {
            initialsText.setText(userName.substring(0, 1).toUpperCase());
        }

        viewModel.getProfileStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null && stats.length >= 3) {
                savedCount.setText(String.valueOf(stats[0]));
                routesCount.setText(String.valueOf(stats[1]));
                alertsCount.setText(String.valueOf(stats[2]));
            }
        });

        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            viewModel.signOut();
            // TODO: Start LoginActivity and finish MainActivity
        });

        view.findViewById(R.id.btn_contribute).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, com.example.project_mobile.ui.contribution.AddStationFragment.newInstance(36.8, 10.2)) // default coords
                .addToBackStack(null)
                .commit();
        });
    }
}
