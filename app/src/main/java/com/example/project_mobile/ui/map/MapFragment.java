package com.example.project_mobile.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.BuildConfig;
import com.example.project_mobile.R;
import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.data.model.RouteResponse;
import com.example.project_mobile.data.remote.RetrofitClient;
import com.example.project_mobile.ui.alerts.AlertsActivity;
import com.example.project_mobile.ui.details.StationDetailsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.plugins.annotation.Line;
import org.maplibre.android.plugins.annotation.LineManager;
import org.maplibre.android.plugins.annotation.LineOptions;
import org.maplibre.android.plugins.annotation.Symbol;
import org.maplibre.android.plugins.annotation.SymbolManager;
import org.maplibre.android.plugins.annotation.SymbolOptions;
import org.maplibre.android.utils.ColorUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapFragment extends Fragment {

    private static final LatLng MOCK_USER_LOCATION = new LatLng(36.81897, 10.16579);
    private static final LatLng DEBUG_TEST_USER_LOCATION = new LatLng(36.8065, 10.1815);

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private SymbolManager symbolManager;
    private LineManager lineManager;
    private Line routeLine;
    private Line reachabilityLine;
    private org.maplibre.android.annotations.Polygon reachabilityPolygon;
    private ChargingStation selectedStation;
    private List<ChargingStation> stations;
    private List<ChargingStation> fullStationList;
    private String currentQuery = "";
    private boolean fAvailable = false, fSlow = false, fSemiFast = false, fFast = false;
    private boolean fMyCar = true;
    private boolean rangeOverlayVisible = false;
    private int currentSoc = 65;
    private boolean alertsVisible = true;
    private LatLng userLocation;
    private boolean usingDebugTestLocation = false;
    private org.maplibre.android.annotations.Marker userLocationMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> fineLocationPermissionLauncher;
    private ChargingStation pendingRouteStation;
    private String pendingRouteStationId;
    private final Map<Long, ChargingStation> symbolStationMap = new HashMap<>();
    private final Map<Long, String> symbolAlertMap = new HashMap<>();
    private final List<org.maplibre.android.annotations.Marker> activeAlertMarkers = new java.util.ArrayList<>();
    private com.example.project_mobile.data.TokenManager tokenManager;
    private String userConnectors = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(requireContext());
        fineLocationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        refreshUserLocation(true);
                    } else {
                        android.widget.Toast.makeText(requireContext(),
                                "Enable location to calculate a road route from your GPS position.",
                                android.widget.Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = root.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        com.example.project_mobile.data.StationRepository repo = new com.example.project_mobile.data.StationRepository(
                requireActivity().getApplication());

        tokenManager = new com.example.project_mobile.data.TokenManager(requireContext());
        userConnectors = tokenManager.getUserConnectors();
        currentSoc = tokenManager.getCurrentSoc();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        refreshUserLocation(false);

        repo.getAllStations().observe(getViewLifecycleOwner(), newStations -> {
            this.fullStationList = newStations;
            performFiltering(currentQuery);
        });

        android.widget.EditText searchInput = view.findViewById(R.id.et_search_map);
        if (searchInput != null) {
            searchInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    currentQuery = s.toString();
                    performFiltering(currentQuery);
                }
            });
        }
        setupFilters(view);
        updateSocSummary(view);

        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            mapLibreMap.setStyle(new Style.Builder().fromUri("asset://osm_raster_style.json"), style -> configureMap());
        });

        View bottomSheet = view.findViewById(R.id.station_preview_sheet);
        bottomSheet.setVisibility(View.GONE);

        view.findViewById(R.id.btn_close_preview).setOnClickListener(v -> {
            android.transition.TransitionManager.beginDelayedTransition((ViewGroup) view);
            bottomSheet.setVisibility(View.GONE);
        });

        view.findViewById(R.id.recenter_button).setOnClickListener(v -> {
            refreshUserLocation(true);
            if (userLocation != null && mapLibreMap != null) {
                mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13.5));
            }
        });
        view.findViewById(R.id.layers_button).setOnClickListener(v -> {
            View legend = view.findViewById(R.id.map_legend);
            legend.setVisibility(legend.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        view.findViewById(R.id.range_button).setOnClickListener(v -> {
            rangeOverlayVisible = !rangeOverlayVisible;
            if (rangeOverlayVisible && getReachableKm() <= 0) {
                android.widget.Toast.makeText(requireContext(), "Set your EV profile to show reachability radius",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
            updateReachabilityOverlay();
            updateSocSummary(view);
        });
        view.findViewById(R.id.btn_set_soc).setOnClickListener(v -> showSocSheet());
        view.findViewById(R.id.zoom_in_button).setOnClickListener(v -> {
            if (mapLibreMap != null) {
                mapLibreMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        view.findViewById(R.id.preview_action).setOnClickListener(v -> {
            if (selectedStation != null) {
                try {
                    int id = Integer.parseInt(selectedStation.id);
                    repo.addFavorite(id, new com.example.project_mobile.data.StationRepository.Callback() {
                        @Override
                        public void onSuccess() {
                            android.widget.Toast.makeText(requireContext(), "Saved to favorites!",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            android.widget.Toast.makeText(requireContext(), "Error saving: " + message,
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (NumberFormatException e) {
                    android.widget.Toast
                            .makeText(requireContext(), "Invalid station ID", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        view.findViewById(R.id.alert_button).setOnClickListener(v -> {
            alertsVisible = !alertsVisible;
            refreshAlertMarkers();
        });
        view.findViewById(R.id.add_alert_button).setOnClickListener(v -> {
            new AddAlertBottomSheet().show(getParentFragmentManager(), "AddAlert");
        });
    }

    private void setupFilters(View v) {
        TextView chipAvailable = v.findViewById(R.id.chip_available);
        TextView chipSlow = v.findViewById(R.id.chip_slow);
        TextView chipSemiFast = v.findViewById(R.id.chip_semifast);
        TextView chipFast = v.findViewById(R.id.chip_fast);
        TextView chipMyCar = v.findViewById(R.id.chip_my_car);

        View.OnClickListener toggle = view -> {
            int id = view.getId();
            if (id == R.id.chip_available)
                fAvailable = !fAvailable;
            else if (id == R.id.chip_slow)
                fSlow = !fSlow;
            else if (id == R.id.chip_semifast)
                fSemiFast = !fSemiFast;
            else if (id == R.id.chip_fast)
                fFast = !fFast;
            else if (id == R.id.chip_my_car)
                fMyCar = !fMyCar;

            updateChipUI((TextView) view, id);
            performFiltering(currentQuery);
        };

        chipAvailable.setOnClickListener(toggle);
        chipSlow.setOnClickListener(toggle);
        chipSemiFast.setOnClickListener(toggle);
        chipFast.setOnClickListener(toggle);
        chipMyCar.setOnClickListener(toggle);
    }

    private void updateChipUI(TextView chip, int id) {
        boolean active = false;
        if (id == R.id.chip_available)
            active = fAvailable;
        else if (id == R.id.chip_slow)
            active = fSlow;
        else if (id == R.id.chip_semifast)
            active = fSemiFast;
        else if (id == R.id.chip_fast)
            active = fFast;
        else if (id == R.id.chip_my_car)
            active = fMyCar;

        chip.setBackgroundResource(active ? R.drawable.bg_filter_chip_active : R.drawable.bg_filter_chip);
        chip.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(),
                active ? R.color.slate_950 : R.color.white));
    }

    private void performFiltering(String query) {
        if (fullStationList == null)
            return;

        String q = (query == null) ? "" : query.toLowerCase(java.util.Locale.US);
        stations = new java.util.ArrayList<>();

        for (ChargingStation s : fullStationList) {
            boolean matchesSearch = q.isEmpty() ||
                    (s.name != null && s.name.toLowerCase(java.util.Locale.US).contains(q)) ||
                    (s.city != null && s.city.toLowerCase(java.util.Locale.US).contains(q));

            if (!matchesSearch)
                continue;

            if (fAvailable && !"Available".equalsIgnoreCase(s.status))
                continue;

            // Speed classification (OR logic) based on GeoJSON CS_Speed attribute
            boolean powerFilterActive = fSlow || fSemiFast || fFast;
            if (powerFilterActive) {
                boolean matchesPower = false;
                String speed = s.csSpeed != null ? s.csSpeed.toUpperCase(java.util.Locale.US) : "";

                if (fSlow && speed.contains("SLOW"))
                    matchesPower = true;
                if (fSemiFast && speed.contains("SEMI-FAST"))
                    matchesPower = true;
                if (fFast && speed.contains("FAST") && !speed.contains("SEMI-FAST"))
                    matchesPower = true;

                if (!matchesPower)
                    continue;
            }

            // EV-Matched Connector Filtering
            if (fMyCar && userConnectors != null && !userConnectors.isEmpty() && s.connectors != null
                    && !s.connectors.isEmpty()) {
                boolean hasCompatibleConnector = false;
                for (String stationConn : s.connectors) {
                    if (userConnectors.toLowerCase().contains(stationConn.toLowerCase().trim())) {
                        hasCompatibleConnector = true;
                        break;
                    }
                }
                if (!hasCompatibleConnector) {
                    continue; // Auto-hide incompatible stations
                }
            }

            stations.add(s);
        }

        if (mapLibreMap != null) {
            configureMap();
        }
        if (pendingRouteStationId != null) {
            selectStationAndRoute(pendingRouteStationId);
        }
    }

    private int parsePower(String powerStr) {
        if (powerStr == null)
            return 0;
        try {
            String numeric = powerStr.replaceAll("[^0-9]", "");
            return numeric.isEmpty() ? 0 : Integer.parseInt(numeric);
        } catch (Exception e) {
            return 0;
        }
    }

    private int getReachableKm() {
        int rangeKm = tokenManager != null ? tokenManager.getRangeWltpKm() : 0;
        if (rangeKm <= 0)
            return 0;
        return Math.max(0, Math.round(rangeKm * (currentSoc / 100f) * 0.85f));
    }

    private void updateSocSummary(View root) {
        TextView summary = root.findViewById(R.id.soc_summary);
        if (summary == null)
            return;
        String vehicle = tokenManager != null ? tokenManager.getVehicleLabel() : "";
        int reachableKm = getReachableKm();
        if (reachableKm > 0) {
            String label = vehicle == null || vehicle.isEmpty() ? "My EV" : vehicle;
            summary.setText(label + " - SoC " + currentSoc + "% - safe radius " + reachableKm + " km");
        } else {
            summary.setText("Set your EV profile to unlock reachability and car-matched stations");
        }
    }

    private void showSocSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext());
        LinearLayout sheet = new LinearLayout(requireContext());
        sheet.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (22 * getResources().getDisplayMetrics().density);
        sheet.setPadding(pad, pad, pad, pad);
        sheet.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.zid_back));

        TextView title = new TextView(requireContext());
        title.setText("Battery level");
        title.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white));
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        sheet.addView(title);

        TextView value = new TextView(requireContext());
        value.setText(currentSoc + "%");
        value.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.teal_300));
        value.setTextSize(36);
        value.setTypeface(null, android.graphics.Typeface.BOLD);
        sheet.addView(value);

        android.widget.SeekBar seekBar = new android.widget.SeekBar(requireContext());
        seekBar.setMax(100);
        seekBar.setProgress(currentSoc);
        sheet.addView(seekBar);

        Button apply = new Button(requireContext());
        apply.setText("Update reachability");
        apply.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.slate_950));
        apply.setBackgroundResource(R.drawable.bg_filter_chip_active);
        sheet.addView(apply);

        seekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                currentSoc = Math.max(5, progress);
                value.setText(currentSoc + "%");
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
            }
        });

        apply.setOnClickListener(v -> {
            if (getView() != null)
                updateSocSummary(getView());
            updateReachabilityOverlay();

            tokenManager.saveCurrentSoc(currentSoc);
            com.example.project_mobile.data.remote.RetrofitClient.getApiService(requireContext())
                    .updateProfile(new com.example.project_mobile.data.remote.UpdateProfileRequest(null, currentSoc))
                    .enqueue(new retrofit2.Callback<com.example.project_mobile.data.remote.UserMeResponse>() {
                        @Override
                        public void onResponse(
                                retrofit2.Call<com.example.project_mobile.data.remote.UserMeResponse> call,
                                retrofit2.Response<com.example.project_mobile.data.remote.UserMeResponse> response) {
                            if (response.isSuccessful()) {
                                android.widget.Toast.makeText(requireContext(), "SoC updated in account!",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                retrofit2.Call<com.example.project_mobile.data.remote.UserMeResponse> call,
                                Throwable t) {
                            // Handle failure
                        }
                    });

            dialog.dismiss();
        });

        dialog.setContentView(sheet);
        dialog.show();
    }

    private void configureMap() {
        if (mapLibreMap == null)
            return;
        Style style = mapLibreMap.getStyle();
        if (style == null)
            return;

        if (symbolManager == null) {
            symbolManager = new SymbolManager(mapView, mapLibreMap, style);
            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);
            symbolManager.addClickListener(symbol -> {
                String alertMsg = symbolAlertMap.get(symbol.getId());
                if (alertMsg != null) {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(),
                            R.style.AppAlertTheme)
                            .setTitle(symbol.getTextField())
                            .setMessage(alertMsg)
                            .setPositiveButton(R.string.action_ok, null)
                            .show();
                    return true;
                }
                return false;
            });
        }

        if (lineManager == null) {
            lineManager = new LineManager(mapView, mapLibreMap, style);
        }

        mapLibreMap.getUiSettings().setCompassEnabled(false);
        mapLibreMap.getUiSettings().setAttributionEnabled(false);
        mapLibreMap.getUiSettings().setLogoEnabled(false);

        mapLibreMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(36.84, 10.22))
                .zoom(10.8)
                .build());

        // Clear existing markers
        mapLibreMap.clear();
        symbolStationMap.clear();
        symbolManager.deleteAll();

        if (stations != null && !stations.isEmpty()) {
            org.maplibre.android.annotations.IconFactory iconFactory = org.maplibre.android.annotations.IconFactory
                    .getInstance(requireContext());
            for (ChargingStation station : stations) {
                int iconResId = markerIconFor(station.status);
                android.graphics.Bitmap bitmap = getBitmapFromVector(iconResId);
                org.maplibre.android.annotations.Icon icon = null;
                if (bitmap != null) {
                    icon = iconFactory.fromBitmap(bitmap);
                }

                org.maplibre.android.annotations.MarkerOptions options = new org.maplibre.android.annotations.MarkerOptions()
                        .position(new LatLng(station.latitude, station.longitude))
                        .title(station.name)
                        .snippet(station.status + " - " + station.power);

                if (icon != null) {
                    options.icon(icon);
                }

                org.maplibre.android.annotations.Marker marker = mapLibreMap.addMarker(options);
                symbolStationMap.put(marker.getId(), station);
            }
        }

        mapLibreMap.setOnMarkerClickListener(marker -> {
            ChargingStation station = symbolStationMap.get(marker.getId());
            if (station != null && getView() != null) {
                bindSelectedStation(getView(), station);
                return true;
            }

            String alertMsg = symbolAlertMap.get(marker.getId());
            if (alertMsg != null) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(),
                        R.style.AppAlertTheme)
                        .setTitle(marker.getTitle())
                        .setMessage(alertMsg)
                        .setPositiveButton(R.string.action_ok, null)
                        .show();
                return true;
            }
            return false;
        });

        updateUserLocationMarker();

        refreshAlertMarkers();

        mapLibreMap.addOnMapClickListener(point -> {
            if (getView() != null) {
                View sheet = getView().findViewById(R.id.station_preview_sheet);
                if (sheet.getVisibility() == View.VISIBLE) {
                    android.transition.TransitionManager.beginDelayedTransition((ViewGroup) getView());
                    sheet.setVisibility(View.GONE);
                }
            }
            return false;
        });

        updateMapSelection();
        updateReachabilityOverlay();
        if (pendingRouteStation != null && userLocation != null) {
            ChargingStation station = pendingRouteStation;
            pendingRouteStation = null;
            requestRouteToStation(station);
        }
    }

    private void handleCheckIn(ChargingStation station, boolean isStarting) {
        if (station == null)
            return;
        com.example.project_mobile.data.StationRepository repo = new com.example.project_mobile.data.StationRepository(
                requireActivity().getApplication());
        repo.checkIn(Integer.parseInt(station.id), isStarting,
                new com.example.project_mobile.data.StationRepository.Callback() {
                    @Override
                    public void onSuccess() {
                        android.widget.Toast.makeText(requireContext(),
                                isStarting ? "Checked in! +10 Points" : "Checked out! +5 Points",
                                android.widget.Toast.LENGTH_SHORT).show();
                        if (isStarting) {
                            float capacity = tokenManager != null ? tokenManager.getBatteryCapacity() : 0f;
                            int powerKw = station.powerKw > 0 ? station.powerKw : parsePower(station.power);
                            int minutes = 30;
                            if (capacity > 0 && powerKw > 0) {
                                float hours = (capacity * 0.8f) / powerKw;
                                minutes = Math.max(1, (int) (hours * 60));
                            }
                            repo.saveSession(Integer.parseInt(station.id), station.name,
                                    station.city != null ? station.city : "", false, 0f, minutes);
                        }
                        if (getView() != null) {
                            getView().findViewById(R.id.station_preview_sheet).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        android.widget.Toast.makeText(requireContext(), "Check-in failed: " + message,
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindSelectedStation(View root, ChargingStation station) {
        selectedStation = station;
        ((TextView) root.findViewById(R.id.preview_title)).setText(station.name);

        String address = firstNonEmpty(station.address, station.city, "Tunisia");
        if (station.governorate != null && !station.governorate.isEmpty() && !address.contains(station.governorate)) {
            address += ", " + station.governorate;
        }
        ((TextView) root.findViewById(R.id.preview_subtitle)).setText(address);

        TextView opView = root.findViewById(R.id.preview_operator);
        if (station.operator != null && !station.operator.isEmpty()) {
            opView.setVisibility(View.VISIBLE);
            String opText = station.operator;
            if (station.verified) {
                opText += " ✓ Verified";
            }
            opView.setText(opText);
        } else {
            opView.setVisibility(View.GONE);
        }

        TextView statusChip = root.findViewById(R.id.preview_status);
        String status = firstNonEmpty(station.status, "Unknown");
        statusChip.setText(status);
        if ("Available".equalsIgnoreCase(status)) {
            statusChip.setBackgroundResource(R.drawable.bg_green_chip);
        } else if ("Busy".equalsIgnoreCase(status)) {
            statusChip.setBackgroundResource(R.drawable.bg_orange_chip);
        } else {
            statusChip.setBackgroundResource(R.drawable.bg_grey_chip);
        }

        TextView routeTag = root.findViewById(R.id.route_tag);
        if (station.distance != null && station.eta != null) {
            routeTag.setVisibility(View.VISIBLE);
            routeTag.setText("Route - " + station.distance + " - " + station.eta);
        } else {
            routeTag.setVisibility(View.GONE);
        }

        String distTime = (station.distance != null ? station.distance : "---") + " • "
                + (station.eta != null ? station.eta : "-- min");
        ((TextView) root.findViewById(R.id.preview_route)).setText(distTime);

        String displayPower = station.powerKw > 0 ? station.powerKw + " kW" : firstNonEmpty(station.power, "Unknown kW");
        ((TextView) root.findViewById(R.id.preview_power)).setText(displayPower + " - " + firstNonEmpty(station.ports, "ports unknown"));

        if (station.connectors != null && !station.connectors.isEmpty()) {
            ((TextView) root.findViewById(R.id.preview_connectors))
                    .setText(android.text.TextUtils.join(" • ", station.connectors));
        } else {
            ((TextView) root.findViewById(R.id.preview_connectors)).setText("");
        }

        TextView estimatorView = root.findViewById(R.id.preview_estimator);
        float capacity = tokenManager != null ? tokenManager.getBatteryCapacity() : 0f;
        int powerKw = station.powerKw > 0 ? station.powerKw : parsePower(station.power);
        String estimatorText = "";

        float vehicleDcMax = tokenManager != null ? tokenManager.getDcMaxPowerKw() : 0f;
        int effectivePowerKw = powerKw;
        if (vehicleDcMax > 0 && powerKw > 0) {
            effectivePowerKw = Math.round(Math.min(vehicleDcMax, powerKw));
        }
        if (capacity > 0 && effectivePowerKw > 0) {
            float targetDelta = Math.max(0.1f, (80 - currentSoc) / 100f);
            float hours = (capacity * targetDelta) / effectivePowerKw;
            int minutes = Math.max(1, (int) (hours * 60));
            estimatorText = "Time to 80%: " + minutes + " min";
        }

        if (station.price != null && !station.price.isEmpty() && !station.price.equals("Unknown")) {
            if (!estimatorText.isEmpty())
                estimatorText += " • ";
            estimatorText += station.price;
        }

        TextView carFit = root.findViewById(R.id.preview_car_fit);
        String vehicle = tokenManager != null ? tokenManager.getVehicleLabel() : "";
        if (vehicle != null && !vehicle.isEmpty()) {
            boolean compatible = isCompatibleWithUserCar(station);
            String speedText = effectivePowerKw > 0 ? effectivePowerKw + " kW max here" : "speed unknown";
            String fitStatus = compatible ? "compatible" : "connector not matched";
            carFit.setVisibility(View.VISIBLE);
            carFit.setText("For your " + vehicle + ": " + fitStatus + " - " + speedText);
        } else {
            carFit.setVisibility(View.VISIBLE);
            carFit.setText("Add your EV profile to show compatible plugs and real charge speed.");
        }

        if (!estimatorText.isEmpty()) {
            estimatorView.setVisibility(View.VISIBLE);
            estimatorView.setText(estimatorText);
        } else {
            estimatorView.setVisibility(View.GONE);
        }

        ImageView previewImage = root.findViewById(R.id.preview_image);
        if (station.imageUrl != null && !station.imageUrl.isEmpty()) {
            previewImage.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(this)
                    .load(station.imageUrl)
                    .into(previewImage);
        } else {
            previewImage.setVisibility(View.GONE);
        }

        Button checkInBtn = root.findViewById(R.id.check_in_action);
        if ("Available".equalsIgnoreCase(status)) {
            checkInBtn.setText("I'm Charging Here");
            checkInBtn.setOnClickListener(v -> requestRouteToStation(station));
        } else {
            checkInBtn.setText("I'm Leaving (Mark Available)");
            checkInBtn.setOnClickListener(v -> handleCheckIn(station, false));
        }

        updateMapSelection();

        View bottomSheet = root.findViewById(R.id.station_preview_sheet);
        if (bottomSheet.getVisibility() != View.VISIBLE) {
            android.transition.TransitionManager.beginDelayedTransition((ViewGroup) root);
            bottomSheet.setVisibility(View.VISIBLE);
        }
    }

    private boolean isCompatibleWithUserCar(ChargingStation station) {
        if (userConnectors == null || userConnectors.isEmpty())
            return true;
        if (station.connectors == null || station.connectors.isEmpty())
            return true;
        for (String stationConn : station.connectors) {
            if (userConnectors.toLowerCase(java.util.Locale.US)
                    .contains(stationConn.toLowerCase(java.util.Locale.US).trim())) {
                return true;
            }
        }
        return false;
    }

    private void refreshAlertMarkers() {
        if (mapLibreMap == null)
            return;

        // Remove existing markers
        for (org.maplibre.android.annotations.Marker m : activeAlertMarkers) {
            mapLibreMap.removeMarker(m);
        }
        activeAlertMarkers.clear();
        symbolAlertMap.clear();

        if (!alertsVisible)
            return;

        org.maplibre.android.annotations.IconFactory iconFactory = org.maplibre.android.annotations.IconFactory
                .getInstance(requireContext());
        android.graphics.Bitmap alertBitmap = getBitmapFromVector(R.drawable.ic_alert_warning);
        org.maplibre.android.annotations.Icon alertIcon = (alertBitmap != null) ? iconFactory.fromBitmap(alertBitmap)
                : null;

        com.example.project_mobile.data.remote.RetrofitClient.getApiService(requireContext())
            .getAlerts()
            .enqueue(new retrofit2.Callback<List<com.example.project_mobile.data.remote.CommunityAlert>>() {
                @Override
                public void onResponse(retrofit2.Call<List<com.example.project_mobile.data.remote.CommunityAlert>> call, retrofit2.Response<List<com.example.project_mobile.data.remote.CommunityAlert>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (com.example.project_mobile.data.remote.CommunityAlert alert : response.body()) {
                            if (alert.isValidated && alert.isActive) {
                                org.maplibre.android.annotations.MarkerOptions options = new org.maplibre.android.annotations.MarkerOptions()
                                    .position(new LatLng(alert.latitude, alert.longitude))
                                    .title(alert.alertType)
                                    .snippet(alert.description);

                                if (alertIcon != null) options.icon(alertIcon);
                                
                                if (mapLibreMap != null) {
                                    org.maplibre.android.annotations.Marker m = mapLibreMap.addMarker(options);
                                    activeAlertMarkers.add(m);
                                    symbolAlertMap.put(m.getId(), alert.description);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<List<com.example.project_mobile.data.remote.CommunityAlert>> call, Throwable t) {
                    // Fail silently
                }
            });
    }

    public void selectStationAndRoute(String stationId) {
        if (stationId == null) {
            pendingRouteStationId = stationId;
            return;
        }
        java.util.List<ChargingStation> source = fullStationList != null ? fullStationList : stations;
        if (source == null) {
            pendingRouteStationId = stationId;
            return;
        }
        for (ChargingStation s : source) {
            if (stationId.equals(s.id)) {
                pendingRouteStationId = null;
                if (getView() != null) {
                    bindSelectedStation(getView(), s);
                    requestRouteToStation(s);
                } else {
                    selectedStation = s;
                    pendingRouteStation = s;
                }
                break;
            }
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

    private void refreshUserLocation(boolean requestIfMissingPermission) {
        if (fusedLocationClient == null || getContext() == null)
            return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (requestIfMissingPermission && fineLocationPermissionLauncher != null) {
                fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            return;
        }

        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(current -> {
            if (current != null) {
                applyUserLocation(current);
            } else {
                useCachedLocationFallback(requestIfMissingPermission);
            }
        }).addOnFailureListener(error -> useCachedLocationFallback(requestIfMissingPermission));
    }

    private void useCachedLocationFallback(boolean showNoFixMessage) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        applyUserLocation(location);
                    } else if (showNoFixMessage) {
                        android.widget.Toast.makeText(requireContext(),
                                "No GPS fix. Set a location in Emulator > Extended Controls > Location, or start routing in debug to use the test origin.",
                                android.widget.Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void applyUserLocation(android.location.Location location) {
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        usingDebugTestLocation = false;
        updateUserLocationMarker();
        if (pendingRouteStation != null) {
            ChargingStation station = pendingRouteStation;
            pendingRouteStation = null;
            requestRouteToStation(station);
        }
    }

    private void applyDebugLocationFallback() {
        if (BuildConfig.DEBUG) {
            userLocation = DEBUG_TEST_USER_LOCATION;
            usingDebugTestLocation = true;
            updateUserLocationMarker();
            android.widget.Toast.makeText(requireContext(),
                    "Debug test origin enabled: Tunis center. Set emulator GPS to test another start point.",
                    android.widget.Toast.LENGTH_LONG).show();
            if (pendingRouteStation != null) {
                ChargingStation station = pendingRouteStation;
                pendingRouteStation = null;
                requestRouteToStation(station);
            }
        } else {
            android.widget.Toast.makeText(requireContext(),
                    "No GPS fix. Enable location services and try again.",
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void updateUserLocationMarker() {
        if (mapLibreMap == null)
            return;
        if (userLocationMarker != null) {
            mapLibreMap.removeMarker(userLocationMarker);
            userLocationMarker = null;
        }
        if (userLocation == null) {
            return;
        }
        userLocationMarker = mapLibreMap.addMarker(new org.maplibre.android.annotations.MarkerOptions()
                .position(userLocation)
                .title(usingDebugTestLocation
                        ? "Debug test origin"
                        : (userLocation != null ? "Your GPS position" : "Tunisia fallback position")));
    }

    private void requestRouteToStation(ChargingStation station) {
        if (station == null)
            return;
        if (BuildConfig.DEBUG && !usingDebugTestLocation) {
            pendingRouteStation = station;
            applyDebugLocationFallback();
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            pendingRouteStation = station;
            fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        if (userLocation == null) {
            pendingRouteStation = station;
            refreshUserLocation(true);
            android.widget.Toast.makeText(requireContext(),
                    "Getting your GPS position for the optimized route...",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        if (mapLibreMap == null || lineManager == null) {
            pendingRouteStation = station;
            return;
        }

        selectedStation = station;
        TextView routeTag = getView() != null ? getView().findViewById(R.id.route_tag) : null;
        if (routeTag != null) {
            routeTag.setVisibility(View.VISIBLE);
            routeTag.setText("Calculating road route...");
        }

        RetrofitClient.getApiService(requireContext())
                .getRoute(userLocation.getLatitude(), userLocation.getLongitude(),
                        station.latitude, station.longitude)
                .enqueue(new retrofit2.Callback<RouteResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<RouteResponse> call,
                                           retrofit2.Response<RouteResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            showRoutingError("Routing failed: " + response.code());
                            return;
                        }
                        RouteResponse route = response.body();
                        java.util.List<LatLng> routePoints = decodePolyline(route.polyline);
                        if (routePoints.isEmpty()) {
                            showRoutingError("Routing failed: empty route");
                            return;
                        }
                        drawRoute(routePoints);
                        String distance = formatDistance(route.distanceM);
                        String eta = formatDuration(route.durationS);
                        updateRouteLabels(distance, eta);
                        saveRouteSession(station, route.durationS);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<RouteResponse> call, Throwable t) {
                        showRoutingError("Routing failed: " + t.getMessage());
                    }
                });
    }

    private void drawRoute(java.util.List<LatLng> routePoints) {
        if (lineManager == null || mapLibreMap == null)
            return;
        if (routeLine != null) {
            lineManager.delete(routeLine);
        }
        routeLine = lineManager.create(new LineOptions()
                .withLatLngs(routePoints)
                .withLineColor(ColorUtils.colorToRgbaString(
                        ContextCompat.getColor(requireContext(), R.color.teal_500)))
                .withLineWidth(5f));

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (LatLng point : routePoints) {
            bounds.include(point);
        }
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 160));
    }

    private void updateRouteLabels(String distance, String eta) {
        if (getView() == null)
            return;
        TextView routeTag = getView().findViewById(R.id.route_tag);
        TextView previewRoute = getView().findViewById(R.id.preview_route);
        if (routeTag != null) {
            routeTag.setVisibility(View.VISIBLE);
            routeTag.setText("Smart route - " + eta + " - " + distance);
        }
        if (previewRoute != null) {
            previewRoute.setText(distance + " - " + eta);
        }
    }

    private void saveRouteSession(ChargingStation station, long durationS) {
        try {
            new com.example.project_mobile.data.StationRepository(requireActivity().getApplication())
                    .saveSession(Integer.parseInt(station.id), station.name,
                            station.city != null ? station.city : "", true, 0f,
                            Math.max(1, Math.round(durationS / 60f)));
        } catch (NumberFormatException ignored) {
            new com.example.project_mobile.data.StationRepository(requireActivity().getApplication())
                    .saveSession(0, station.name, station.city != null ? station.city : "",
                            true, 0f, Math.max(1, Math.round(durationS / 60f)));
        }
    }

    private void showRoutingError(String message) {
        if (getView() != null) {
            TextView routeTag = getView().findViewById(R.id.route_tag);
            if (routeTag != null) {
                routeTag.setVisibility(View.VISIBLE);
                routeTag.setText("Route unavailable");
            }
        }
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show();
    }

    private String formatDistance(double meters) {
        if (meters >= 1000) {
            return String.format(java.util.Locale.US, "%.1f km", meters / 1000d);
        }
        return Math.round(meters) + " m";
    }

    private String formatDuration(long seconds) {
        long minutes = Math.max(1, Math.round(seconds / 60f));
        if (minutes < 60) {
            return minutes + " min";
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return remainingMinutes == 0 ? hours + " h" : hours + " h " + remainingMinutes + " min";
    }

    private java.util.List<LatLng> decodePolyline(String encoded) {
        java.util.List<LatLng> polyline = new java.util.ArrayList<>();
        if (encoded == null || encoded.isEmpty())
            return polyline;

        int index = 0;
        int lat = 0;
        int lng = 0;
        while (index < encoded.length()) {
            int[] latitudeResult = decodeNextPolylineValue(encoded, index);
            lat += latitudeResult[0];
            index = latitudeResult[1];
            if (index >= encoded.length())
                break;
            int[] longitudeResult = decodeNextPolylineValue(encoded, index);
            lng += longitudeResult[0];
            index = longitudeResult[1];
            polyline.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return polyline;
    }

    private int[] decodeNextPolylineValue(String encoded, int startIndex) {
        int result = 0;
        int shift = 0;
        int index = startIndex;
        int b;
        do {
            b = encoded.charAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20 && index < encoded.length());
        int value = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
        return new int[]{value, index};
    }

    private int markerIconFor(String status) {
        if (status == null) {
            return R.drawable.ic_ev_station_inactive;
        }
        String normalized = status.toLowerCase(java.util.Locale.US);
        if (normalized.contains("available") || normalized.contains("ready") || normalized.contains("open")) {
            return R.drawable.ic_ev_station_available;
        }
        if (normalized.contains("busy") || normalized.contains("charging") || normalized.contains("occupied")) {
            return R.drawable.ic_ev_station_busy;
        }
        if (normalized.contains("offline") || normalized.contains("down") || normalized.contains("maintenance")) {
            return R.drawable.ic_ev_station_inactive;
        }
        return R.drawable.ic_ev_station_inactive;
    }

    private void updateMapSelection() {
        if (mapLibreMap == null || selectedStation == null || lineManager == null) {
            return;
        }

        LatLng origin = userLocation != null ? userLocation : MOCK_USER_LOCATION;
        LatLng stationLatLng = new LatLng(selectedStation.latitude, selectedStation.longitude);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(origin)
                .include(stationLatLng)
                .build();

        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 180));
    }

    private void updateReachabilityOverlay() {
        if (lineManager == null)
            return;
        if (reachabilityLine != null) {
            lineManager.delete(reachabilityLine);
            reachabilityLine = null;
        }
        if (reachabilityPolygon != null) {
            mapLibreMap.removePolygon(reachabilityPolygon);
            reachabilityPolygon = null;
        }
        if (!rangeOverlayVisible)
            return;
        int radiusKm = getReachableKm();
        if (radiusKm <= 0)
            return;

        java.util.List<LatLng> circle = new java.util.ArrayList<>();
        double lat = MOCK_USER_LOCATION.getLatitude();
        double lon = MOCK_USER_LOCATION.getLongitude();
        double radiusEarthKm = 6371.0;
        double angularDistance = radiusKm / radiusEarthKm;
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        for (int i = 0; i <= 72; i++) {
            double bearing = Math.toRadians(i * 5.0);
            double pointLat = Math.asin(Math.sin(latRad) * Math.cos(angularDistance)
                    + Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(bearing));
            double pointLon = lonRad + Math.atan2(Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(latRad),
                    Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(pointLat));
            circle.add(new LatLng(Math.toDegrees(pointLat), Math.toDegrees(pointLon)));
        }

        // Add filled polygon for background
        reachabilityPolygon = mapLibreMap.addPolygon(new org.maplibre.android.annotations.PolygonOptions()
                .addAll(circle)
                .fillColor(android.graphics.Color.argb(10, 255, 255, 0)) // Transparent yellow
                .strokeColor(android.graphics.Color.TRANSPARENT));

        // Add line for border
        reachabilityLine = lineManager.create(new LineOptions()
                .withLatLngs(circle)
                .withLineColor("#f3ffb0ff")
                .withLineWidth(4f)
                .withLineOpacity(0.9f));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mapView != null) {
            mapView.onStop();
        }
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    private android.graphics.Bitmap getBitmapFromVector(int resId) {
        android.graphics.drawable.Drawable vectorDrawable = androidx.core.content.ContextCompat
                .getDrawable(requireContext(), resId);
        if (vectorDrawable == null) {
            return null;
        }
        int width = vectorDrawable.getIntrinsicWidth() > 0 ? vectorDrawable.getIntrinsicWidth() : 64;
        int height = vectorDrawable.getIntrinsicHeight() > 0 ? vectorDrawable.getIntrinsicHeight() : 64;
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height,
                android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
        if (symbolManager != null) {
            symbolManager.onDestroy();
            symbolManager = null;
        }
        if (lineManager != null) {
            lineManager.onDestroy();
            lineManager = null;
        }
        mapLibreMap = null;
        routeLine = null;
        super.onDestroyView();
    }
}
