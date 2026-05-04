package com.example.project_mobile.ui.contribution;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_mobile.R;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

public class MapPickerActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LNG = "extra_lng";

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LatLng selectedLatLng = new LatLng(36.8, 10.2); // Default to Tunis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_map_picker);

        findViewById(R.id.picker_toolbar).setOnClickListener(v -> finish());

        TextView coordsText = findViewById(R.id.picker_coords_text);

        mapView = findViewById(R.id.picker_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            map.setStyle(new Style.Builder().fromUri("asset://osm_raster_style.json"), style -> {
                map.setCameraPosition(new CameraPosition.Builder()
                        .target(selectedLatLng)
                        .zoom(10.0)
                        .build());

                map.addOnCameraMoveListener(() -> {
                    selectedLatLng = map.getCameraPosition().target;
                    coordsText.setText(String.format("Lat: %.5f, Lng: %.5f", selectedLatLng.getLatitude(), selectedLatLng.getLongitude()));
                });
            });
        });

        findViewById(R.id.btn_confirm_location).setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_LAT, selectedLatLng.getLatitude());
            result.putExtra(EXTRA_LNG, selectedLatLng.getLongitude());
            setResult(RESULT_OK, result);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
