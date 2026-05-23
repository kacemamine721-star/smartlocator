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
    private static final String GEOJSON_FILE = "ev_stations_tunisia"; // res/raw/ev_stations_tunisia.geojson

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
                e.origin = props.get("ORIGIN") != null && !props.get("ORIGIN").isJsonNull()
                        ? props.get("ORIGIN").getAsString() : "";
                        
                e.price = "Unknown";
                if (props.has("charging_free") && !props.get("charging_free").isJsonNull()) {
                    e.price = props.get("charging_free").getAsBoolean() ? "Free" : "Paid";
                }
                
                if (props.has("connector_types") && !props.get("connector_types").isJsonNull() && props.get("connector_types").isJsonArray()) {
                    List<String> connList = new ArrayList<>();
                    for (JsonElement connEl : props.getAsJsonArray("connector_types")) {
                        connList.add(connEl.getAsString());
                    }
                    e.connectors = String.join(",", connList);
                } else {
                    e.connectors = "Type2";
                }
                e.longitude = coords.get(0).getAsDouble();
                e.latitude = coords.get(1).getAsDouble();
                e.isFavorite = false;
                e.averageRating = 0f;
                e.ratingCount = 0;
                e.isUserContributed = false;

                // Enriched properties
                e.powerKw = props.has("power_kw") && !props.get("power_kw").isJsonNull() ? props.get("power_kw").getAsInt() : 0;
                e.operator = props.has("operator") && !props.get("operator").isJsonNull() ? props.get("operator").getAsString() : "";
                e.operatorType = props.has("operator_type") && !props.get("operator_type").isJsonNull() ? props.get("operator_type").getAsString() : "";
                e.governorate = props.has("governorate") && !props.get("governorate").isJsonNull() ? props.get("governorate").getAsString() : "";
                e.access = props.has("access") && !props.get("access").isJsonNull() ? props.get("access").getAsString() : "";
                e.verified = props.has("verified") && !props.get("verified").isJsonNull() && props.get("verified").getAsBoolean();
                e.address = e.governorate != null && !e.governorate.isEmpty() ? e.governorate : "Tunisia";
                e.city = e.governorate != null && !e.governorate.isEmpty() ? e.governorate : "Tunisia";
                e.power = e.powerKw > 0 ? e.powerKw + " kW" : e.csSpeed;
                e.network = e.operator != null && !e.operator.isEmpty() ? e.operator : "Community";
                e.reliability = e.verified ? "Verified" : "Community reported";

                entities.add(e);
            }
            dao.insertAll(entities);
            Log.d(TAG, "Seeded " + entities.size() + " stations from GeoJSON.");
        } catch (Exception ex) {
            Log.e(TAG, "Failed to load GeoJSON: " + ex.getMessage(), ex);
        }
    }
}
