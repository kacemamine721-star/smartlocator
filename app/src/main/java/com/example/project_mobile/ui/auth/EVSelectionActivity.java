package com.example.project_mobile.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_mobile.MainActivity;
import com.example.project_mobile.R;
import com.example.project_mobile.data.TokenManager;
import com.example.project_mobile.data.remote.EVVehicle;
import com.example.project_mobile.data.remote.RetrofitClient;
import com.example.project_mobile.data.remote.UpdateProfileRequest;
import com.example.project_mobile.data.remote.UserMeResponse;
import com.example.project_mobile.ui.common.EvImageLoader;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EVSelectionActivity extends AppCompatActivity {

    private RecyclerView rvEvList;
    private MaterialButton btnConfirm;
    private EVAdapter adapter;
    private List<EVVehicle> vehicles = new ArrayList<>();
    private EVVehicle selectedVehicle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ev_selection);

        rvEvList = findViewById(R.id.rv_ev_list);
        rvEvList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EVAdapter();
        rvEvList.setAdapter(adapter);

        btnConfirm = findViewById(R.id.btn_confirm_ev);
        btnConfirm.setOnClickListener(v -> confirmSelection());

        findViewById(R.id.tv_skip).setOnClickListener(v -> {
            openMain();
        });

        loadVehicles();
    }

    private void loadVehicles() {
        RetrofitClient.getApiService(this).getVehicles().enqueue(new Callback<List<EVVehicle>>() {
            @Override
            public void onResponse(@NonNull Call<List<EVVehicle>> call, @NonNull Response<List<EVVehicle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vehicles = response.body();
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(EVSelectionActivity.this, "Failed to load vehicles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EVVehicle>> call, @NonNull Throwable t) {
                Toast.makeText(EVSelectionActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmSelection() {
        if (selectedVehicle == null) return;
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Updating...");

        RetrofitClient.getApiService(this).updateProfile(new UpdateProfileRequest(selectedVehicle.id))
                .enqueue(new Callback<UserMeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserMeResponse> call, @NonNull Response<UserMeResponse> response) {
                        if (response.isSuccessful()) {
                            String acConn = selectedVehicle.ac_connector_type != null ? selectedVehicle.ac_connector_type : "";
                            String dcConn = selectedVehicle.dc_connector_type != null ? selectedVehicle.dc_connector_type : "";
                            float capacity = selectedVehicle.usable_capacity_kwh != null ? selectedVehicle.usable_capacity_kwh : 0f;
                            int range = selectedVehicle.range_wltp_km != null ? selectedVehicle.range_wltp_km : 0;
                            float dcPower = selectedVehicle.dc_max_power_kw != null ? selectedVehicle.dc_max_power_kw : 0f;
                            int kmPerHourDc = selectedVehicle.km_per_hour_dc != null ? selectedVehicle.km_per_hour_dc : 0;
                            new TokenManager(EVSelectionActivity.this).saveVehicleSpecs(
                                    selectedVehicle.brand + " " + selectedVehicle.model_name,
                                    capacity,
                                    range,
                                    dcPower,
                                    kmPerHourDc,
                                    acConn + "," + dcConn
                            );
                            openMain();
                        } else {
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("Confirm Selection");
                            Toast.makeText(EVSelectionActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserMeResponse> call, @NonNull Throwable t) {
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText("Confirm Selection");
                        Toast.makeText(EVSelectionActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private class EVAdapter extends RecyclerView.Adapter<EVAdapter.EVViewHolder> {

        @NonNull
        @Override
        public EVViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ev_vehicle, parent, false);
            return new EVViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EVViewHolder holder, int position) {
            EVVehicle vehicle = vehicles.get(position);
            holder.tvBrand.setText(vehicle.brand);
            holder.tvModel.setText(vehicle.model_name);

            boolean isSelected = vehicle == selectedVehicle;
            holder.itemView.setBackgroundColor(isSelected ? Color.parseColor("#162831") : Color.TRANSPARENT);
            holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            EvImageLoader.load(holder.ivImage, vehicle.image);

            holder.itemView.setOnClickListener(v -> {
                selectedVehicle = vehicle;
                btnConfirm.setEnabled(true);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return vehicles.size();
        }

        class EVViewHolder extends RecyclerView.ViewHolder {
            TextView tvBrand;
            TextView tvModel;
            ImageView ivSelected;
            ImageView ivImage;

            public EVViewHolder(@NonNull View itemView) {
                super(itemView);
                tvBrand = itemView.findViewById(R.id.tv_ev_brand);
                tvModel = itemView.findViewById(R.id.tv_ev_model);
                ivSelected = itemView.findViewById(R.id.iv_ev_selected);
                ivImage = itemView.findViewById(R.id.iv_ev_image);
            }
        }
    }
}
