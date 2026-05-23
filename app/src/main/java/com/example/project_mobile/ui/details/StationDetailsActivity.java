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
import com.example.project_mobile.data.TokenManager;
import com.example.project_mobile.ui.common.EvImageLoader;

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
        intent.putExtra("longitude", station.longitude);
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
        TokenManager tokenManager = new TokenManager(this);

        findViewById(R.id.details_back).setOnClickListener(v -> finish());

        String name = getIntent().getStringExtra("name");
        String city = getIntent().getStringExtra("city");
        String idString = getIntent().getStringExtra("id");

        ((TextView) findViewById(R.id.details_title)).setText(firstNonEmpty(name, "Charging station"));
        ((TextView) findViewById(R.id.details_address)).setText(firstNonEmpty(getIntent().getStringExtra("address"), city, "Tunisia"));
        ((TextView) findViewById(R.id.details_status)).setText(firstNonEmpty(getIntent().getStringExtra("status"), "Unknown"));
        ((TextView) findViewById(R.id.details_route)).setText(firstNonEmpty(getIntent().getStringExtra("distance"), "---")
                + " - " + firstNonEmpty(getIntent().getStringExtra("eta"), "-- min"));
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

        ((TextView) findViewById(R.id.details_ports)).setText(firstNonEmpty(getIntent().getStringExtra("ports"), "ports unknown"));
        ((TextView) findViewById(R.id.details_hours)).setText(firstNonEmpty(getIntent().getStringExtra("hours"), "Hours unknown"));
        ((TextView) findViewById(R.id.details_price)).setText(firstNonEmpty(getIntent().getStringExtra("price"), "Price unknown"));
        ((TextView) findViewById(R.id.details_reliability)).setText(firstNonEmpty(getIntent().getStringExtra("reliability"), "Reliability unknown"));
        ArrayList<String> connectors = getIntent().getStringArrayListExtra("connectors");
        ((TextView) findViewById(R.id.details_connectors)).setText(
                connectors != null && !connectors.isEmpty()
                        ? android.text.TextUtils.join(" - ", connectors)
                        : "Connectors unknown");
        bindForMyCarPanel(tokenManager, powerKw, getIntent().getStringExtra("power"),
                getIntent().getStringArrayListExtra("connectors"));

        String imageUrl = getIntent().getStringExtra("imageUrl");
        ImageView stationImageView = findViewById(R.id.details_station_image);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            stationImageView.setVisibility(android.view.View.VISIBLE);
            EvImageLoader.loadRemote(stationImageView, imageUrl);
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
                submitView.setEnabled(false);
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
                            submitView.setEnabled(true);
                            Toast.makeText(StationDetailsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    submitView.setEnabled(true);
                    Toast.makeText(this, "Invalid ID", Toast.LENGTH_SHORT).show();
                }
            });
            
            dialog.show();
        });

        Button btnGoCharge = findViewById(R.id.btn_go_charge);
        btnGoCharge.setOnClickListener(v -> {
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mainIntent.putExtra("routing_station_id", idString);
            startActivity(mainIntent);
            finish();
        });
    }

    private void bindForMyCarPanel(TokenManager tokenManager, int stationPowerKw, String stationPowerText,
                                   ArrayList<String> stationConnectors) {
        String vehicle = tokenManager.getVehicleLabel();
        TextView title = findViewById(R.id.details_my_car_title);
        TextView speed = findViewById(R.id.details_my_car_speed);
        TextView time = findViewById(R.id.details_my_car_time);
        TextView connector = findViewById(R.id.details_my_car_connector);

        if (vehicle == null || vehicle.isEmpty()) {
            title.setText("Add your EV profile");
            speed.setText("Unlock compatible-station filtering and realistic charge speed.");
            time.setText("The app uses your battery size and DC limit from the EV database.");
            connector.setText("Vehicle can be selected after sign-in.");
            return;
        }

        int parsedPower = stationPowerKw > 0 ? stationPowerKw : parsePower(stationPowerText);
        float vehicleDc = tokenManager.getDcMaxPowerKw();
        int effectivePower = parsedPower;
        if (vehicleDc > 0 && parsedPower > 0) {
            effectivePower = Math.round(Math.min(vehicleDc, parsedPower));
        }
        float capacity = tokenManager.getBatteryCapacity();
        int minutes = 0;
        if (capacity > 0 && effectivePower > 0) {
            minutes = Math.max(1, Math.round((capacity * 0.70f / effectivePower) * 60f));
        }

        boolean compatible = isCompatible(tokenManager.getUserConnectors(), stationConnectors);
        title.setText(vehicle);
        speed.setText(effectivePower > 0
                ? "Max achievable speed here: " + effectivePower + " kW"
                : "Max achievable speed here: unknown");
        time.setText(minutes > 0
                ? "10% to 80%: about " + minutes + " minutes"
                : "10% to 80%: add battery data to estimate");
        connector.setText("Connector: " + (compatible ? "compatible" : "not matched to your EV"));
    }

    private boolean isCompatible(String userConnectors, ArrayList<String> stationConnectors) {
        if (userConnectors == null || userConnectors.isEmpty()) return true;
        if (stationConnectors == null || stationConnectors.isEmpty()) return true;
        String normalized = userConnectors.toLowerCase(java.util.Locale.US);
        for (String stationConnector : stationConnectors) {
            if (stationConnector != null && normalized.contains(stationConnector.toLowerCase(java.util.Locale.US).trim())) {
                return true;
            }
        }
        return false;
    }

    private int parsePower(String powerText) {
        if (powerText == null) return 0;
        try {
            String numeric = powerText.replaceAll("[^0-9]", "");
            return numeric.isEmpty() ? 0 : Integer.parseInt(numeric);
        } catch (Exception e) {
            return 0;
        }
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }
}
