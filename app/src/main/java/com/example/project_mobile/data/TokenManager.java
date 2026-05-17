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

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public boolean hasToken() {
        return getAccessToken() != null;
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
