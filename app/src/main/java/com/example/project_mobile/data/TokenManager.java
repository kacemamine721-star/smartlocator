package com.example.project_mobile.data;

import android.content.Context;
import android.content.SharedPreferences;

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

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveTokens(String access, String refresh) {
        prefs.edit().putString(KEY_ACCESS, access)
                    .putString(KEY_REFRESH, refresh).apply();
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

    public void clear() {
        prefs.edit().clear().apply();
    }
}
