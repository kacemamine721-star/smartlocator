package com.example.project_mobile.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

/**
 * Stores and retrieves JWT tokens in SharedPreferences.
 * All teammates call this to get the Bearer token for API requests.
 */
public class TokenManager {

    private static final String PREFS_NAME = "smartlocator_prefs";
    private static final String KEY_ACCESS  = "jwt_access";
    private static final String KEY_REFRESH = "jwt_refresh";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_CONNECTORS = "user_connectors";
    private static final String KEY_BATTERY_CAPACITY = "battery_capacity_kwh";
    private static final String KEY_VEHICLE_LABEL = "vehicle_label";
    private static final String KEY_RANGE_WLTP_KM = "range_wltp_km";
    private static final String KEY_DC_MAX_POWER_KW = "dc_max_power_kw";
    private static final String KEY_KM_PER_HOUR_DC = "km_per_hour_dc";
    private static final String KEY_CURRENT_SOC = "current_soc";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveTokens(String access, String refresh) {
        prefs.edit().putString(KEY_ACCESS, access)
                    .putString(KEY_REFRESH, refresh)
                    .putString(KEY_USER_ID, extractUserId(access))
                    .apply();
    }

    public void saveAccessToken(String access) {
        prefs.edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_USER_ID, extractUserId(access))
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public boolean hasToken() {
        return getAccessToken() != null;
    }

    public boolean isAccessTokenExpired() {
        String access = getAccessToken();
        if (access == null || access.isEmpty()) {
            return true;
        }
        try {
            String[] parts = access.split("\\.");
            if (parts.length < 2) {
                return true;
            }
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            JSONObject payload = new JSONObject(new String(decoded, java.nio.charset.StandardCharsets.UTF_8));
            long expSeconds = payload.optLong("exp", 0);
            long nowSeconds = System.currentTimeMillis() / 1000L;
            return expSeconds <= nowSeconds + 30;
        } catch (Exception ignored) {
            return true;
        }
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "local_user");
    }

    public void saveUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Driver");
    }

    public void saveUserConnectors(String connectors) {
        prefs.edit().putString(KEY_USER_CONNECTORS, connectors).apply();
    }

    public String getUserConnectors() {
        return prefs.getString(KEY_USER_CONNECTORS, "");
    }

    public void saveBatteryCapacity(float capacity) {
        prefs.edit().putFloat(KEY_BATTERY_CAPACITY, capacity).apply();
    }

    public float getBatteryCapacity() {
        return prefs.getFloat(KEY_BATTERY_CAPACITY, 0f);
    }

    public void saveVehicleSpecs(String label, float batteryCapacityKwh, int rangeWltpKm,
                                 float dcMaxPowerKw, int kmPerHourDc, String connectors) {
        prefs.edit()
                .putString(KEY_VEHICLE_LABEL, label)
                .putFloat(KEY_BATTERY_CAPACITY, batteryCapacityKwh)
                .putInt(KEY_RANGE_WLTP_KM, rangeWltpKm)
                .putFloat(KEY_DC_MAX_POWER_KW, dcMaxPowerKw)
                .putInt(KEY_KM_PER_HOUR_DC, kmPerHourDc)
                .putString(KEY_USER_CONNECTORS, connectors)
                .apply();
    }

    public String getVehicleLabel() {
        return prefs.getString(KEY_VEHICLE_LABEL, "");
    }

    public int getRangeWltpKm() {
        return prefs.getInt(KEY_RANGE_WLTP_KM, 0);
    }

    public float getDcMaxPowerKw() {
        return prefs.getFloat(KEY_DC_MAX_POWER_KW, 0f);
    }

    public int getKmPerHourDc() {
        return prefs.getInt(KEY_KM_PER_HOUR_DC, 0);
    }

    public void saveCurrentSoc(int soc) {
        prefs.edit().putInt(KEY_CURRENT_SOC, soc).apply();
    }

    public int getCurrentSoc() {
        return prefs.getInt(KEY_CURRENT_SOC, 65);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    private String extractUserId(String access) {
        try {
            String[] parts = access.split("\\.");
            if (parts.length < 2) {
                return "local_user";
            }
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            JSONObject payload = new JSONObject(new String(decoded, java.nio.charset.StandardCharsets.UTF_8));
            String userId = payload.optString("user_id", "");
            return userId.isEmpty() ? "local_user" : userId;
        } catch (Exception ignored) {
            return "local_user";
        }
    }
}
