package com.example.project_mobile.ui.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        intent.putExtra("longitude", station.longitude);
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
        ((TextView) findViewById(R.id.details_provider)).setText(getIntent().getStringExtra("provider"));
        ((TextView) findViewById(R.id.details_power)).setText(getIntent().getStringExtra("power"));
        ((TextView) findViewById(R.id.details_ports)).setText(getIntent().getStringExtra("ports"));
        ((TextView) findViewById(R.id.details_hours)).setText(getIntent().getStringExtra("hours"));
        ((TextView) findViewById(R.id.details_price)).setText(getIntent().getStringExtra("price"));
        ((TextView) findViewById(R.id.details_reliability)).setText(getIntent().getStringExtra("reliability"));
        ((TextView) findViewById(R.id.details_connectors)).setText(android.text.TextUtils.join(" - ", getIntent().getStringArrayListExtra("connectors")));

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
