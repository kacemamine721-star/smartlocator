package com.example.project_mobile.data.local;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads EV_CHARGING_STATIONS_TUNISIA.geojson from res/raw on first launch
 * and seeds the Room stations table.
 * Must be called on a background thread.
 */
public class GeoJsonLoader {

    private static final String TAG = "GeoJsonLoader";
    private static final String GEOJSON_FILE = "ev_stations"; // res/raw/ev_stations.geojson

    /**
     * Seeds the DB if empty. Call from a background thread (e.g. Executors.newSingleThreadExecutor).
     */
    public static void seedIfEmpty(Context context, StationDao dao) {
        if (dao.count() > 0) {
            Log.d(TAG, "DB already seeded, skipping.");
            return;
        }
        try {
            int resId = context.getResources().getIdentifier(
                    GEOJSON_FILE, "raw", context.getPackageName());
            if (resId == 0) {
                Log.e(TAG, "ev_stations.geojson not found in res/raw/");
                return;
            }
            InputStream is = context.getResources().openRawResource(resId);
            JsonObject root = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
            JsonArray features = root.getAsJsonArray("features");

            List<StationEntity> entities = new ArrayList<>();
            for (JsonElement el : features) {
                JsonObject feature = el.getAsJsonObject();
                JsonObject props = feature.getAsJsonObject("properties");
                JsonArray coords = feature.getAsJsonObject("geometry")
                        .getAsJsonArray("coordinates");

                StationEntity e = new StationEntity();
                e.id = props.get("id").getAsInt();
                e.name = props.get("NAME").isJsonNull()
                        ? "Station " + e.id
                        : props.get("NAME").getAsString();
                e.status = props.get("STATUS").isJsonNull()
                        ? "UNKNOWN"
                        : props.get("STATUS").getAsString();
                e.reportUs = props.get("report_us").isJsonNull()
                        ? ""
                        : props.get("report_us").getAsString();
                e.csSpeed = props.get("CS_Speed").isJsonNull()
                        ? ""
                        : props.get("CS_Speed").getAsString();
                e.origin = props.get("ORIGIN").isJsonNull()
                        ? ""
                        : props.get("ORIGIN").getAsString();
                e.longitude = coords.get(0).getAsDouble();
                e.latitude = coords.get(1).getAsDouble();
                e.isFavorite = false;
                e.averageRating = 0f;
                e.ratingCount = 0;
                e.isUserContributed = false;

                entities.add(e);
            }
            dao.insertAll(entities);
            Log.d(TAG, "Seeded " + entities.size() + " stations from GeoJSON.");
        } catch (Exception ex) {
            Log.e(TAG, "Failed to load GeoJSON: " + ex.getMessage(), ex);
        }
    }
}
