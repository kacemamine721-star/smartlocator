package com.example.project_mobile.ui.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;
import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.data.MockStationRepository;
import com.example.project_mobile.ui.alerts.AlertsActivity;
import com.example.project_mobile.ui.details.StationDetailsActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.Polyline;
import org.maplibre.android.annotations.PolylineOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private static final LatLng MOCK_USER_LOCATION = new LatLng(36.81897, 10.16579);

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private Polyline routePolyline;
    private ChargingStation selectedStation;
    private List<ChargingStation> stations;
    private final Map<Long, ChargingStation> markerStationMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = root.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        com.example.project_mobile.data.StationRepository repo = new com.example.project_mobile.data.StationRepository(requireActivity().getApplication());
        repo.getAllStations().observe(getViewLifecycleOwner(), newStations -> {
            stations = newStations;
            if (stations != null && !stations.isEmpty()) {
                if (selectedStation == null) selectedStation = stations.get(0);
                if (mapLibreMap != null) configureMap();
            }
        });

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
                            android.widget.Toast.makeText(requireContext(), "Saved to favorites!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String message) {
                            android.widget.Toast.makeText(requireContext(), "Error saving: " + message, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (NumberFormatException e) {
                    android.widget.Toast.makeText(requireContext(), "Invalid station ID", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        view.findViewById(R.id.alert_button).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AlertsActivity.class)));
    }

    private void configureMap() {
        if (mapLibreMap == null) {
            return;
        }

        mapLibreMap.getUiSettings().setCompassEnabled(false);
        mapLibreMap.getUiSettings().setAttributionEnabled(false);
        mapLibreMap.getUiSettings().setLogoEnabled(false);

        mapLibreMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(36.84, 10.22))
                .zoom(10.8)
                .build());

        if (mapLibreMap.getMarkers() != null) {
            java.util.List<org.maplibre.android.annotations.Marker> markers = new java.util.ArrayList<>(mapLibreMap.getMarkers());
            for (org.maplibre.android.annotations.Marker m : markers) {
                mapLibreMap.removeMarker(m);
            }
        }
        markerStationMap.clear();
        if (stations != null) {
            for (ChargingStation station : stations) {
            Marker marker = mapLibreMap.addMarker(new MarkerOptions()
                    .position(new LatLng(station.latitude, station.longitude))
                    .title(station.name)
                    .snippet(station.status + " - " + station.power));
            markerStationMap.put(marker.getId(), station);
        }
        }

        Marker userMarker = mapLibreMap.addMarker(new MarkerOptions()
                .position(MOCK_USER_LOCATION)
                .title("Your location")
                .snippet("Current EV position"));
        markerStationMap.remove(userMarker.getId());

        mapLibreMap.setOnMarkerClickListener(marker -> {
            ChargingStation station = markerStationMap.get(marker.getId());
            if (station != null && getView() != null) {
                bindSelectedStation(getView(), station);
                return true;
            }
            return false;
        });

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
        String ratingText = station.ratingCount > 0 ? String.format(java.util.Locale.US, "★ %.1f (%d) • ", station.averageRating, station.ratingCount) : "New • ";
        ((TextView) root.findViewById(R.id.preview_subtitle)).setText(ratingText + station.address);
        ((TextView) root.findViewById(R.id.preview_status)).setText(station.status);
        ((TextView) root.findViewById(R.id.preview_route)).setText(station.distance + " - " + station.eta + " away");
        ((TextView) root.findViewById(R.id.preview_power)).setText(station.power + " - " + station.ports);
        ((TextView) root.findViewById(R.id.preview_connectors)).setText(android.text.TextUtils.join(" - ", station.connectors));
        updateMapSelection();

        View bottomSheet = root.findViewById(R.id.station_preview_sheet);
        if (bottomSheet.getVisibility() != View.VISIBLE) {
            android.transition.TransitionManager.beginDelayedTransition((ViewGroup) root);
            bottomSheet.setVisibility(View.VISIBLE);
        }
    }

    private void updateMapSelection() {
        if (mapLibreMap == null || selectedStation == null) {
            return;
        }

        LatLng stationLatLng = new LatLng(selectedStation.latitude, selectedStation.longitude);
        if (routePolyline != null) {
            mapLibreMap.removePolyline(routePolyline);
        }

        routePolyline = mapLibreMap.addPolyline(new PolylineOptions()
                .add(MOCK_USER_LOCATION)
                .add(stationLatLng)
                .color(requireContext().getColor(R.color.teal_500))
                .width(5f));

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

    @Override
    public void onDestroyView() {
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
        mapLibreMap = null;
        routePolyline = null;
        super.onDestroyView();
    }
}
