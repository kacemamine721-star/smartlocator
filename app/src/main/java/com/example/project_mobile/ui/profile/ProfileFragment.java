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
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.project_mobile.data.remote.RetrofitClient;
import com.example.project_mobile.data.remote.UserMeResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        View llEvVehicle = view.findViewById(R.id.ll_ev_vehicle);
        TextView tvEvBrand = view.findViewById(R.id.tv_profile_ev_brand);
        TextView tvEvModel = view.findViewById(R.id.tv_profile_ev_model);
        ImageView btnChangeEv = view.findViewById(R.id.btn_change_ev);

        if (btnChangeEv != null) {
            btnChangeEv.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), com.example.project_mobile.ui.auth.EVSelectionActivity.class));
            });
        }

        View badgeContainer = view.findViewById(R.id.ll_profile_badge_container);
        if (badgeContainer != null) {
            badgeContainer.setOnClickListener(v -> {
                android.content.SharedPreferences prefs = requireActivity().getPreferences(android.content.Context.MODE_PRIVATE);
                if (prefs.getBoolean("needs_level_up_dialog", false)) {
                    String levelKey = prefs.getString("new_level_key", "rookie");
                    showCustomLevelUpDialog(levelKey);
                    prefs.edit().putBoolean("needs_level_up_dialog", false).apply();
                }
            });
        }

        RetrofitClient.getApiService(requireContext()).getUserMe().enqueue(new Callback<UserMeResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserMeResponse> call, @NonNull Response<UserMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.project_mobile.data.remote.UserProfile profile = response.body().profile;
                    if (profile != null) {
                        TextView badgeLabel = view.findViewById(R.id.profile_badge_label);
                        TextView pointsLabel = view.findViewById(R.id.profile_points_label);
                        ImageView badgeImage = view.findViewById(R.id.profile_badge_image);
                        
                        android.content.SharedPreferences prefs = requireActivity().getPreferences(android.content.Context.MODE_PRIVATE);
                        int lastPoints = prefs.getInt("last_points", 0);
                        int newPoints = profile.points;

                        if (pointsLabel != null) pointsLabel.setText(newPoints + " Points");
                        if (badgeLabel != null) {
                            if (newPoints < 50) {
                                badgeLabel.setText(getString(R.string.badge_rookie));
                            } else if (newPoints < 150) {
                                badgeLabel.setText(getString(R.string.badge_power));
                            } else {
                                badgeLabel.setText(getString(R.string.badge_master));
                            }
                        }
                        
                        if (badgeImage != null) {
                            if (newPoints < 50) {
                                badgeImage.setImageResource(R.drawable.badge_bronze);
                            } else if (newPoints < 150) {
                                badgeImage.setImageResource(R.drawable.badge_silver);
                            } else {
                                badgeImage.setImageResource(R.drawable.badge_gold);
                            }
                        }

                        // Level up detection
                        if (lastPoints < 50 && newPoints >= 50) {
                            prefs.edit().putBoolean("needs_level_up_dialog", true).putString("new_level_key", "power").apply();
                        } else if (lastPoints < 150 && newPoints >= 150) {
                            prefs.edit().putBoolean("needs_level_up_dialog", true).putString("new_level_key", "master").apply();
                        }

                        // Save new points
                        prefs.edit().putInt("last_points", newPoints).apply();

                        if (profile.vehicle != null && llEvVehicle != null) {
                            llEvVehicle.setVisibility(View.VISIBLE);
                            if (tvEvBrand != null) tvEvBrand.setText(profile.vehicle.brand);
                            if (tvEvModel != null) tvEvModel.setText(profile.vehicle.model_name);
                            
                            ImageView ivProfileEv = view.findViewById(R.id.iv_profile_ev);
                            if (ivProfileEv != null) {
                                if (profile.vehicle.image != null) {
                                    Glide.with(requireContext())
                                            .load(profile.vehicle.image)
                                            .into(ivProfileEv);
                                } else {
                                    ivProfileEv.setImageResource(R.drawable.ic_car_charging);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserMeResponse> call, @NonNull Throwable t) {
                // Ignore failure for now
            }
        });

        // Settings Logic
        android.content.SharedPreferences prefs = requireActivity().getPreferences(android.content.Context.MODE_PRIVATE);

        com.google.android.material.switchmaterial.SwitchMaterial ccs2Switch = view.findViewById(R.id.switch_ccs2);
        com.google.android.material.switchmaterial.SwitchMaterial notifAvailSwitch = view.findViewById(R.id.switch_notif_availability);
        com.google.android.material.switchmaterial.SwitchMaterial notifDemandSwitch = view.findViewById(R.id.switch_notif_demand);
        android.widget.Spinner langSpinner = view.findViewById(R.id.spinner_language);

        if (ccs2Switch != null) ccs2Switch.setChecked(prefs.getBoolean("pref_ccs2", false));
        if (notifAvailSwitch != null) notifAvailSwitch.setChecked(prefs.getBoolean("pref_notif_avail", true));
        if (notifDemandSwitch != null) notifDemandSwitch.setChecked(prefs.getBoolean("pref_notif_demand", true));
        if (langSpinner != null) langSpinner.setSelection(prefs.getInt("pref_lang", 0));

        if (ccs2Switch != null) ccs2Switch.setOnCheckedChangeListener((btn, isChecked) -> prefs.edit().putBoolean("pref_ccs2", isChecked).apply());
        if (notifAvailSwitch != null) notifAvailSwitch.setOnCheckedChangeListener((btn, isChecked) -> prefs.edit().putBoolean("pref_notif_avail", isChecked).apply());
        if (notifDemandSwitch != null) notifDemandSwitch.setOnCheckedChangeListener((btn, isChecked) -> prefs.edit().putBoolean("pref_notif_demand", isChecked).apply());
        
        if (langSpinner != null) {
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
        }

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
    
    private void showCustomLevelUpDialog(String levelKey) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_level_up, null);
        
        TextView messageTv = dialogView.findViewById(R.id.dialog_message);
        ImageView badgeIv = dialogView.findViewById(R.id.dialog_badge_image);
        android.widget.Button okBtn = dialogView.findViewById(R.id.dialog_btn_ok);
        
        String levelName = "";
        int imageResId = R.drawable.badge_bronze;
        
        if ("power".equals(levelKey)) {
            levelName = getString(R.string.badge_power);
            imageResId = R.drawable.badge_silver;
        } else if ("master".equals(levelKey)) {
            levelName = getString(R.string.badge_master);
            imageResId = R.drawable.badge_gold;
        } else {
            levelName = getString(R.string.badge_rookie);
            imageResId = R.drawable.badge_bronze;
        }
        
        if (messageTv != null) {
            messageTv.setText(getString(R.string.level_up_message, levelName));
        }
        
        if (badgeIv != null) {
            badgeIv.setImageResource(imageResId);
        }
        
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create();
            
        if (okBtn != null) {
            okBtn.setOnClickListener(v -> dialog.dismiss());
        }
        
        dialog.show();
    }
}
