package com.example.project_mobile.ui.trip;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;
import com.example.project_mobile.MainActivity;
import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.data.StationRepository;
import com.example.project_mobile.data.TokenManager;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TripFragment extends Fragment {

    private TokenManager tokenManager;
    private List<ChargingStation> stations;
    private int currentSoc = 65;

    private TextView vehicleSummary;
    private TextView socLabel;
    private TextView resultTitle;
    private TextView resultBody;
    private TextView stopCard;
    private EditText destinationInput;
    private android.widget.Button openMapButton;
    private ChargingStation suggestedStop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tokenManager = new TokenManager(requireContext());
        currentSoc = tokenManager.getCurrentSoc();
        vehicleSummary = view.findViewById(R.id.trip_vehicle_summary);
        socLabel = view.findViewById(R.id.trip_soc_label);
        resultTitle = view.findViewById(R.id.trip_result_title);
        resultBody = view.findViewById(R.id.trip_result_body);
        stopCard = view.findViewById(R.id.trip_stop_card);
        destinationInput = view.findViewById(R.id.trip_destination_input);
        openMapButton = view.findViewById(R.id.btn_trip_open_map);

        bindVehicleSummary();

        SeekBar socSeek = view.findViewById(R.id.trip_soc_seekbar);
        socSeek.setProgress(currentSoc);
        socSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSoc = Math.max(5, progress);
                socLabel.setText("Battery: " + currentSoc + "%");
                tokenManager.saveCurrentSoc(currentSoc);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        new StationRepository(requireActivity().getApplication()).getAllStations()
                .observe(getViewLifecycleOwner(), value -> stations = value);

        view.findViewById(R.id.btn_plan_trip).setOnClickListener(v -> planTrip());
        openMapButton.setOnClickListener(v -> openSuggestedStopOnMap());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tokenManager != null) {
            currentSoc = tokenManager.getCurrentSoc();
            if (socLabel != null) {
                socLabel.setText("Battery: " + currentSoc + "%");
            }
            SeekBar socSeek = getView().findViewById(R.id.trip_soc_seekbar);
            if (socSeek != null) {
                socSeek.setProgress(currentSoc);
            }
        }
    }

    private void bindVehicleSummary() {
        String vehicle = tokenManager.getVehicleLabel();
        int range = tokenManager.getRangeWltpKm();
        String connectors = tokenManager.getUserConnectors();
        if (vehicle == null || vehicle.isEmpty()) {
            vehicleSummary.setText("Select your EV to unlock car-matched routing.");
        } else {
            vehicleSummary.setText(vehicle + " - " + range + " km WLTP - " + connectors.replace(",", " / "));
        }
    }

    private void planTrip() {
        int distanceKm = estimateDestinationDistance(destinationInput.getText() == null
                ? "" : destinationInput.getText().toString());
        int rangeKm = tokenManager.getRangeWltpKm();
        if (rangeKm <= 0) {
            resultTitle.setText("EV profile required");
            resultBody.setText("Add your vehicle so ZidCharge can use battery range, connector type, and DC charging speed.");
            stopCard.setVisibility(View.GONE);
            openMapButton.setVisibility(View.GONE);
            return;
        }

        int reachableKm = Math.round(rangeKm * (currentSoc / 100f) * 0.85f);
        if (distanceKm <= 0) {
            resultTitle.setText("Destination not recognized");
            resultBody.setText("Try a Tunisian city such as Sousse, Sfax, Bizerte, Nabeul, Kairouan, Gabes, or Djerba.");
            stopCard.setVisibility(View.GONE);
            openMapButton.setVisibility(View.GONE);
            return;
        }

        if (reachableKm >= distanceKm) {
            resultTitle.setText("You can make it");
            resultBody.setText("At " + currentSoc + "%, your safe driving radius is about " + reachableKm
                    + " km. Destination estimate: " + distanceKm + " km.");
            stopCard.setVisibility(View.GONE);
            openMapButton.setVisibility(View.GONE);
        } else {
            ChargingStation stop = bestChargingStop();
            suggestedStop = stop;
            int shortfallKm = distanceKm - reachableKm;
            int kmPerHourDc = tokenManager.getKmPerHourDc();
            int stopMinutes = kmPerHourDc > 0 ? Math.max(8, Math.round((shortfallKm / (float) kmPerHourDc) * 60f)) : 25;
            resultTitle.setText("Charging stop needed");
            resultBody.setText("At " + currentSoc + "%, safe reach is about " + reachableKm
                    + " km. Destination estimate: " + distanceKm + " km, so plan one stop.");
            if (stop != null) {
                String power = stop.powerKw > 0 ? stop.powerKw + " kW" : stop.power;
                stopCard.setText("Suggested stop: " + stop.name + " - " + power
                        + " DC - about " + stopMinutes + " min to continue");
                openMapButton.setVisibility(View.VISIBLE);
            } else {
                stopCard.setText("No compatible station loaded yet. Use the map to contribute or verify stations on this route.");
                openMapButton.setVisibility(View.GONE);
            }
            stopCard.setVisibility(View.VISIBLE);
        }
    }

    private void openSuggestedStopOnMap() {
        if (suggestedStop == null) return;
        Intent mainIntent = new Intent(requireContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mainIntent.putExtra("routing_station_id", suggestedStop.id);
        startActivity(mainIntent);
    }

    private int estimateDestinationDistance(String destination) {
        String key = destination == null ? "" : destination.toLowerCase(Locale.US);
        Map<String, Integer> tunisDistances = new HashMap<>();
        tunisDistances.put("nabeul", 70);
        tunisDistances.put("hammamet", 75);
        tunisDistances.put("bizerte", 72);
        tunisDistances.put("sousse", 145);
        tunisDistances.put("monastir", 170);
        tunisDistances.put("kairouan", 160);
        tunisDistances.put("sfax", 270);
        tunisDistances.put("gabes", 405);
        tunisDistances.put("djerba", 520);
        tunisDistances.put("tozeur", 450);
        for (Map.Entry<String, Integer> entry : tunisDistances.entrySet()) {
            if (key.contains(entry.getKey())) return entry.getValue();
        }
        return 0;
    }

    private ChargingStation bestChargingStop() {
        if (stations == null || stations.isEmpty()) return null;
        ChargingStation best = null;
        int bestPower = -1;
        String connectors = tokenManager.getUserConnectors();
        for (ChargingStation station : stations) {
            if (!isCompatible(connectors, station)) continue;
            int power = station.powerKw > 0 ? station.powerKw : parsePower(station.power);
            if (power > bestPower) {
                bestPower = power;
                best = station;
            }
        }
        return best;
    }

    private boolean isCompatible(String connectors, ChargingStation station) {
        if (connectors == null || connectors.isEmpty()) return true;
        if (station.connectors == null || station.connectors.isEmpty()) return true;
        String normalized = connectors.toLowerCase(Locale.US);
        for (String stationConnector : station.connectors) {
            if (stationConnector != null && normalized.contains(stationConnector.toLowerCase(Locale.US).trim())) {
                return true;
            }
        }
        return false;
    }

    private int parsePower(String value) {
        if (value == null) return 0;
        try {
            String numeric = value.replaceAll("[^0-9]", "");
            return numeric.isEmpty() ? 0 : Integer.parseInt(numeric);
        } catch (Exception e) {
            return 0;
        }
    }
}
