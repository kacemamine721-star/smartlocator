package com.example.project_mobile.ui.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_mobile.MainActivity;
import com.example.project_mobile.R;
import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.data.StationRepository;

import java.util.ArrayList;

public class StationDetailsActivity extends AppCompatActivity {

    public static Intent createIntent(@NonNull Context context, @NonNull ChargingStation station) {
        Intent intent = new Intent(context, StationDetailsActivity.class);
        intent.putExtra("id", station.id);
        intent.putExtra("name", station.name);
        intent.putExtra("address", station.address);
        intent.putExtra("city", station.city);
        intent.putExtra("distance", station.distance);
        intent.putExtra("eta", station.eta);
        intent.putExtra("status", station.status);
        intent.putExtra("power", station.power);
        intent.putExtra("ports", station.ports);
        intent.putExtra("hours", station.hours);
        intent.putExtra("provider", station.provider);
        intent.putExtra("price", station.price);
        intent.putExtra("reliability", station.reliability);
        intent.putExtra("latitude", station.latitude);
        intent.putExtra("operator", station.operator);
        intent.putExtra("verified", station.verified);
        intent.putExtra("access", station.access);
        intent.putExtra("powerKw", station.powerKw);
        intent.putExtra("averageRating", station.averageRating);
        intent.putExtra("ratingCount", station.ratingCount);
        intent.putExtra("userRating", station.userRating != null ? station.userRating : -1);
        intent.putExtra("imageUrl", station.imageUrl);
        intent.putStringArrayListExtra("connectors", new ArrayList<>(station.connectors));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_station_details);

        StationRepository repository = new StationRepository(getApplication());

        findViewById(R.id.details_back).setOnClickListener(v -> finish());

        String name = getIntent().getStringExtra("name");
        String city = getIntent().getStringExtra("city");
        String idString = getIntent().getStringExtra("id");

        ((TextView) findViewById(R.id.details_title)).setText(name);
        ((TextView) findViewById(R.id.details_address)).setText(getIntent().getStringExtra("address"));
        ((TextView) findViewById(R.id.details_status)).setText(getIntent().getStringExtra("status"));
        ((TextView) findViewById(R.id.details_route)).setText(getIntent().getStringExtra("distance") + " - " + getIntent().getStringExtra("eta"));
        String operator = getIntent().getStringExtra("operator");
        boolean verified = getIntent().getBooleanExtra("verified", false);
        String access = getIntent().getStringExtra("access");
        int powerKw = getIntent().getIntExtra("powerKw", 0);
        float avgRating = getIntent().getFloatExtra("averageRating", 0f);
        int ratingCount = getIntent().getIntExtra("ratingCount", 0);

        String opText = operator != null && !operator.isEmpty() ? operator : "Unknown Operator";
        if (verified) {
            opText += " ✓ Verified";
        }
        ((TextView) findViewById(R.id.details_operator)).setText(opText);
        
        String accessText = access != null && !access.isEmpty() ? access : "Public Access";
        ((TextView) findViewById(R.id.details_access)).setText(accessText);
        
        String powerStr = getIntent().getStringExtra("power");
        String displayPower = powerKw > 0 ? powerKw + " kW" : (powerStr != null ? powerStr : "Unknown kW");
        ((TextView) findViewById(R.id.details_power)).setText(displayPower);

        TextView ratingView = findViewById(R.id.details_rating);
        int userRating = getIntent().getIntExtra("userRating", -1);
        if (ratingCount > 0) {
            ratingView.setText(String.format("★ %.1f (%d reviews)", avgRating, ratingCount));
        } else {
            ratingView.setText("No ratings yet");
        }
        
        TextView btnRate = findViewById(R.id.btn_rate_station);
        if (userRating != -1) {
            btnRate.setText("Your Rating: " + userRating + " ★");
        }

        ((TextView) findViewById(R.id.details_ports)).setText(getIntent().getStringExtra("ports"));
        ((TextView) findViewById(R.id.details_hours)).setText(getIntent().getStringExtra("hours"));
        ((TextView) findViewById(R.id.details_price)).setText(getIntent().getStringExtra("price"));
        ((TextView) findViewById(R.id.details_reliability)).setText(getIntent().getStringExtra("reliability"));
        ((TextView) findViewById(R.id.details_connectors)).setText(android.text.TextUtils.join(" - ", getIntent().getStringArrayListExtra("connectors")));

        String imageUrl = getIntent().getStringExtra("imageUrl");
        ImageView stationImageView = findViewById(R.id.details_station_image);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            stationImageView.setVisibility(android.view.View.VISIBLE);
            com.bumptech.glide.Glide.with(this)
                .load(imageUrl)
                .into(stationImageView);
        }

        findViewById(R.id.btn_report_issue).setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(idString);
                repository.flagStation(id, new StationRepository.Callback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(StationDetailsActivity.this, "Station flagged for review", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(StationDetailsActivity.this, "Failed to flag: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Invalid station ID", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_rate_station).setOnClickListener(v -> {
            com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
            android.view.View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_rating, null);
            dialog.setContentView(sheetView);
            
            ((TextView) sheetView.findViewById(R.id.rating_station_name)).setText(name);
            
            final int[] selectedRating = {0};
            int[] starsIds = {R.id.star_1, R.id.star_2, R.id.star_3, R.id.star_4, R.id.star_5};
            
            if (userRating != -1) {
                selectedRating[0] = userRating;
                for (int j = 0; j < starsIds.length; j++) {
                    android.widget.ImageButton btn = sheetView.findViewById(starsIds[j]);
                    if (j < userRating) {
                        btn.setImageResource(android.R.drawable.btn_star_big_on);
                        btn.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.yellow_500));
                    } else {
                        btn.setImageResource(android.R.drawable.btn_star_big_off);
                        btn.setColorFilter(null);
                    }
                }
            }
            
            for (int i = 0; i < starsIds.length; i++) {
                final int ratingValue = i + 1;
                sheetView.findViewById(starsIds[i]).setOnClickListener(starView -> {
                    selectedRating[0] = ratingValue;
                    for (int j = 0; j < starsIds.length; j++) {
                        android.widget.ImageButton btn = sheetView.findViewById(starsIds[j]);
                        if (j < ratingValue) {
                            btn.setImageResource(android.R.drawable.btn_star_big_on);
                            btn.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.yellow_500));
                        } else {
                            btn.setImageResource(android.R.drawable.btn_star_big_off);
                            btn.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.slate_950));
                        }
                    }
                });
            }
            
            sheetView.findViewById(R.id.btn_submit_rating).setOnClickListener(submitView -> {
                if (selectedRating[0] == 0) {
                    Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                    return;
                }
                String comment = ((com.google.android.material.textfield.TextInputEditText) sheetView.findViewById(R.id.rating_comment_input)).getText().toString();
                
                try {
                    int id = Integer.parseInt(idString);
                    repository.submitRating(id, selectedRating[0], comment, new StationRepository.Callback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(StationDetailsActivity.this, "Rating submitted!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(StationDetailsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(this, "Invalid ID", Toast.LENGTH_SHORT).show();
                }
            });
            
            dialog.show();
        });

        Button btnGoCharge = findViewById(R.id.btn_go_charge);
        btnGoCharge.setOnClickListener(v -> {
            // 1. Collect for history
            try {
                int id = Integer.parseInt(idString);
                repository.saveSession(id, name, city, true, 0, 0);
            } catch (Exception e) {
                repository.saveSession(0, name, city, true, 0, 0);
            }
            Toast.makeText(this, "Route added to history", Toast.LENGTH_SHORT).show();

            // 2. Navigate back to MainActivity and trigger internal routing
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mainIntent.putExtra("routing_station_id", idString);
            startActivity(mainIntent);
            finish();
        });
    }
}
