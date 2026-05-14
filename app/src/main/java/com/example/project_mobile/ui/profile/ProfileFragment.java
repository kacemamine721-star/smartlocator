package com.example.project_mobile.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;
import com.example.project_mobile.ui.auth.WelcomeActivity;

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

        // Settings Logic
        android.content.SharedPreferences prefs = requireActivity().getPreferences(android.content.Context.MODE_PRIVATE);

        com.google.android.material.switchmaterial.SwitchMaterial ccs2Switch = view.findViewById(R.id.switch_ccs2);
        com.google.android.material.switchmaterial.SwitchMaterial notifAvailSwitch = view.findViewById(R.id.switch_notif_availability);
        com.google.android.material.switchmaterial.SwitchMaterial notifDemandSwitch = view.findViewById(R.id.switch_notif_demand);
        android.widget.Spinner langSpinner = view.findViewById(R.id.spinner_language);

        ccs2Switch.setChecked(prefs.getBoolean("pref_ccs2", false));
        notifAvailSwitch.setChecked(prefs.getBoolean("pref_notif_avail", true));
        notifDemandSwitch.setChecked(prefs.getBoolean("pref_notif_demand", true));
        langSpinner.setSelection(prefs.getInt("pref_lang", 0));

        ccs2Switch.setOnCheckedChangeListener((btn, isChecked) -> prefs.edit().putBoolean("pref_ccs2", isChecked).apply());
        notifAvailSwitch.setOnCheckedChangeListener((btn, isChecked) -> prefs.edit().putBoolean("pref_notif_avail", isChecked).apply());
        notifDemandSwitch.setOnCheckedChangeListener((btn, isChecked) -> prefs.edit().putBoolean("pref_notif_demand", isChecked).apply());
        
        langSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (prefs.getInt("pref_lang", 0) != position) {
                    prefs.edit().putInt("pref_lang", position).apply();
                    String langCode = position == 1 ? "fr" : (position == 2 ? "ar" : "en");
                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                        androidx.core.os.LocaleListCompat.forLanguageTags(langCode)
                    );
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            viewModel.signOut();
            Intent intent = new Intent(requireContext(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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
