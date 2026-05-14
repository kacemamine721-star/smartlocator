package com.example.project_mobile.ui.map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;
import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.ui.alerts.AlertsActivity;
import com.example.project_mobile.ui.details.StationDetailsActivity;
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

import org.maplibre.android.annotations.Icon;
import org.maplibre.android.annotations.IconFactory;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapFragment extends Fragment {

    private static final LatLng MOCK_USER_LOCATION = new LatLng(36.81897, 10.16579);

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private SymbolManager symbolManager;
    private LineManager lineManager;
    private Line routeLine;
    private ChargingStation selectedStation;
    private List<ChargingStation> stations;
    private List<ChargingStation> fullStationList;
    private String currentQuery = "";
    private boolean fAvailable = false, fSlow = false, fSemiFast = false, fFast = false;
    private boolean alertsVisible = true;
    private final Map<Long, ChargingStation> symbolStationMap = new HashMap<>();
    private final Map<Long, String> symbolAlertMap = new HashMap<>();
    private final List<Marker> activeAlertMarkers = new java.util.ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(requireContext());
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
        
        repo.getAllStations().observe(getViewLifecycleOwner(), newStations -> {
            this.fullStationList = newStations;
            performFiltering(currentQuery); 
        });

        android.widget.EditText searchInput = view.findViewById(R.id.et_search_map);
        if (searchInput != null) {
            searchInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(android.text.Editable s) {
                    currentQuery = s.toString();
                    performFiltering(currentQuery);
                }
            });
        }
        setupFilters(view);

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

        view.findViewById(R.id.recenter_button).setOnClickListener(v -> updateMapSelection());
        view.findViewById(R.id.layers_button).setOnClickListener(v -> {
            View legend = view.findViewById(R.id.map_legend);
            legend.setVisibility(legend.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
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
        TextView chipFast = v.findViewById(R.id.chip_fast);
        TextView chipType2 = v.findViewById(R.id.chip_type2);
        TextView chipOpen = v.findViewById(R.id.chip_open);

        View.OnClickListener toggle = view -> {
            int id = view.getId();
            if (id == R.id.chip_available) fAvailable = !fAvailable;
            else if (id == R.id.chip_fast) fSlow = !fSlow;
            else if (id == R.id.chip_type2) fSemiFast = !fSemiFast;
            else if (id == R.id.chip_open) fFast = !fFast;

            updateChipUI((TextView) view, id);
            performFiltering(currentQuery);
        };

        chipAvailable.setOnClickListener(toggle);
        chipFast.setOnClickListener(toggle);
        chipType2.setOnClickListener(toggle);
        chipOpen.setOnClickListener(toggle);
    }

    private void updateChipUI(TextView chip, int id) {
        boolean active = false;
        if (id == R.id.chip_available) active = fAvailable;
        else if (id == R.id.chip_fast) active = fSlow;
        else if (id == R.id.chip_type2) active = fSemiFast;
        else if (id == R.id.chip_open) active = fFast;

        chip.setBackgroundResource(active ? R.drawable.bg_filter_chip_active : R.drawable.bg_filter_chip);
        chip.setTextColor(active ? getResources().getColor(R.color.slate_950, null) : getResources().getColor(R.color.white, null));
    }

    private void performFiltering(String query) {
        if (fullStationList == null) return;
        
        String q = (query == null) ? "" : query.toLowerCase(java.util.Locale.US);
        stations = new java.util.ArrayList<>();
        
        for (ChargingStation s : fullStationList) {
            boolean matchesSearch = q.isEmpty() || 
                (s.name != null && s.name.toLowerCase(java.util.Locale.US).contains(q)) ||
                (s.city != null && s.city.toLowerCase(java.util.Locale.US).contains(q));
            
            if (!matchesSearch) continue;
            
            if (fAvailable && !"Available".equalsIgnoreCase(s.status)) continue;
            
            // Speed classification (OR logic) based on GeoJSON CS_Speed attribute
            boolean powerFilterActive = fSlow || fSemiFast || fFast;
            if (powerFilterActive) {
                boolean matchesPower = false;
                String speed = s.csSpeed != null ? s.csSpeed.toUpperCase(java.util.Locale.US) : "";
                
                if (fSlow && speed.contains("SLOW")) matchesPower = true;
                if (fSemiFast && speed.contains("SEMI-FAST")) matchesPower = true;
                if (fFast && speed.contains("FAST") && !speed.contains("SEMI-FAST")) matchesPower = true;
                
                if (!matchesPower) continue;
            }
            
            stations.add(s);
        }
        
        if (mapLibreMap != null) {
            configureMap();
        }
    }

    private int parsePower(String powerStr) {
        if (powerStr == null) return 0;
        try {
            String numeric = powerStr.replaceAll("[^0-9]", "");
            return numeric.isEmpty() ? 0 : Integer.parseInt(numeric);
        } catch (Exception e) {
            return 0;
        }
    }

    private void configureMap() {
        if (mapLibreMap == null) return;
        Style style = mapLibreMap.getStyle();
        if (style == null) return;

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
            IconFactory iconFactory = IconFactory.getInstance(requireContext());
            for (ChargingStation station : stations) {
                int iconResId = markerIconFor(station.status);
                android.graphics.Bitmap bitmap = getBitmapFromVector(iconResId);
                Icon icon = null;
                if (bitmap != null) {
                    icon = iconFactory.fromBitmap(bitmap);
                }

                MarkerOptions options = new MarkerOptions()
                        .position(new LatLng(station.latitude, station.longitude))
                        .title(station.name)
                        .snippet(station.status + " - " + station.power);
                
                if (icon != null) {
                    options.icon(icon);
                }

                Marker marker = mapLibreMap.addMarker(options);
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

        // User location marker
        mapLibreMap.addMarker(new MarkerOptions()
                .position(MOCK_USER_LOCATION)
                .title("Your location"));

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
    }

    private void bindSelectedStation(View root, ChargingStation station) {
        selectedStation = station;
        ((TextView) root.findViewById(R.id.preview_title)).setText(station.name);
        
        String address = station.address != null ? station.address : station.city;
        ((TextView) root.findViewById(R.id.preview_subtitle)).setText(address);
        
        TextView statusChip = root.findViewById(R.id.preview_status);
        statusChip.setText(station.status);
        if ("Available".equalsIgnoreCase(station.status)) {
            statusChip.setBackgroundResource(R.drawable.bg_green_chip);
        } else if ("Busy".equalsIgnoreCase(station.status)) {
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

        String distTime = (station.distance != null ? station.distance : "---") + " • " + (station.eta != null ? station.eta : "-- min");
        ((TextView) root.findViewById(R.id.preview_route)).setText(distTime);
        
        ((TextView) root.findViewById(R.id.preview_power)).setText(station.power + " • " + station.ports);
        
        if (station.connectors != null && !station.connectors.isEmpty()) {
            ((TextView) root.findViewById(R.id.preview_connectors)).setText(android.text.TextUtils.join(" • ", station.connectors));
        }

        updateMapSelection();

        View bottomSheet = root.findViewById(R.id.station_preview_sheet);
        if (bottomSheet.getVisibility() != View.VISIBLE) {
            android.transition.TransitionManager.beginDelayedTransition((ViewGroup) root);
            bottomSheet.setVisibility(View.VISIBLE);
        }
    }

    private void refreshAlertMarkers() {
        if (mapLibreMap == null) return;

        // Remove existing markers
        for (Marker m : activeAlertMarkers) {
            mapLibreMap.removeMarker(m);
        }
        activeAlertMarkers.clear();
        symbolAlertMap.clear();

        if (!alertsVisible) return;

        IconFactory iconFactory = IconFactory.getInstance(requireContext());
        android.graphics.Bitmap alertBitmap = getBitmapFromVector(R.drawable.ic_alert_warning);
        Icon alertIcon = (alertBitmap != null) ? iconFactory.fromBitmap(alertBitmap) : null;

        // Mock alerts for now (later fetch from backend)
        MarkerOptions highDemand = new MarkerOptions()
                .position(new LatLng(36.832, 10.231))
                .title("High Demand")
                .snippet("Near Les Berges du Lac. Nearby chargers are about 90% occupied.");
        
        if (alertIcon != null) highDemand.icon(alertIcon);
        Marker s1 = mapLibreMap.addMarker(highDemand);
        activeAlertMarkers.add(s1);
        symbolAlertMap.put(s1.getId(), highDemand.getSnippet());

        MarkerOptions maintenance = new MarkerOptions()
                .position(new LatLng(36.845, 10.210))
                .title("Maintenance")
                .snippet("Station AGIL Lac 2 is undergoing maintenance until 6 PM.");
        
        if (alertIcon != null) maintenance.icon(alertIcon);
        Marker s2 = mapLibreMap.addMarker(maintenance);
        activeAlertMarkers.add(s2);
        symbolAlertMap.put(s2.getId(), maintenance.getSnippet());
    }

    public void selectStationAndRoute(String stationId) {
        if (stations == null || stationId == null)
            return;
        for (ChargingStation s : stations) {
            if (stationId.equals(s.id)) {
                if (getView() != null) {
                    bindSelectedStation(getView(), s);
                } else {
                    selectedStation = s;
                }
                break;
            }
        }
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

        LatLng stationLatLng = new LatLng(selectedStation.latitude, selectedStation.longitude);
        if (routeLine != null) {
            lineManager.delete(routeLine);
        }

        routeLine = lineManager.create(new LineOptions()
                .withLatLngs(java.util.Arrays.asList(MOCK_USER_LOCATION, stationLatLng))
                .withLineColor(ColorUtils.colorToRgbaString(
                        androidx.core.content.ContextCompat.getColor(requireContext(), R.color.teal_500)))
                .withLineWidth(5f));

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(MOCK_USER_LOCATION)
                .include(stationLatLng)
                .build();

        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 180));
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
