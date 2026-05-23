package com.example.project_mobile.data.remote;

import android.content.Context;

import com.google.gson.Gson;
import com.example.project_mobile.data.TokenManager;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    // Senior Dev Optimization: Using BuildConfig to switch between local and production URLs automatically
    private static final String BASE_URL = com.example.project_mobile.BuildConfig.BASE_URL;
    private static final int CACHE_SIZE_BYTES = 10 * 1024 * 1024;
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static synchronized ApiService getApiService(Context context) {
        if (apiService == null) {
            Context appContext = context.getApplicationContext();
            TokenManager tokenManager = new TokenManager(appContext);
            Cache cache = new Cache(appContext.getCacheDir(), CACHE_SIZE_BYTES);
            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .connectTimeout(12, TimeUnit.SECONDS)
                    .readTimeout(25, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(35, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        String token = tokenManager.getAccessToken();
                        okhttp3.Request request = chain.request();
                        if (isCacheablePublicGet(request)) {
                            request = request.newBuilder()
                                    .cacheControl(new CacheControl.Builder()
                                            .maxAge(2, TimeUnit.MINUTES)
                                            .build())
                                    .build();
                        }
                        if (!isPublicRequest(request) && tokenManager.isAccessTokenExpired()) {
                            token = refreshAccessToken(tokenManager);
                        }
                        if (token != null && !token.isEmpty() && !isPublicRequest(request)) {
                            request = request.newBuilder()
                                    .addHeader("Authorization", "Bearer " + token)
                                    .build();
                        }
                        return chain.proceed(request);
                    })
                    .addNetworkInterceptor(chain -> {
                        okhttp3.Response response = chain.proceed(chain.request());
                        if (isCacheablePublicGet(chain.request())) {
                            return response.newBuilder()
                                    .header("Cache-Control", "public, max-age=120, stale-if-error=86400")
                                    .build();
                        }
                        return response;
                    })
                    .authenticator((route, response) -> {
                        if (response.request().header("Authorization") == null || responseCount(response) >= 2) {
                            return null;
                        }
                        String refreshed = refreshAccessToken(tokenManager);
                        if (refreshed == null || refreshed.isEmpty()) {
                            tokenManager.clear();
                            return null;
                        }
                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + refreshed)
                                .build();
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    private static boolean isPublicGetRequest(okhttp3.Request request) {
        if (!"GET".equalsIgnoreCase(request.method())) {
            return false;
        }
        String path = request.url().encodedPath();
        return path.endsWith("/api/stations/")
                || path.contains("/api/stations/")
                || path.endsWith("/api/alerts/")
                || path.endsWith("/api/route/");
    }

    private static boolean isPublicRequest(okhttp3.Request request) {
        String path = request.url().encodedPath();
        return isPublicGetRequest(request)
                || path.endsWith("/api/auth/login/")
                || path.endsWith("/api/auth/register/")
                || path.endsWith("/api/auth/refresh/");
    }

    private static boolean isCacheablePublicGet(okhttp3.Request request) {
        if (!"GET".equalsIgnoreCase(request.method())) {
            return false;
        }
        String path = request.url().encodedPath();
        return path.endsWith("/api/stations/")
                || path.endsWith("/api/vehicles/")
                || path.endsWith("/api/alerts/");
    }

    private static synchronized String refreshAccessToken(TokenManager tokenManager) {
        String refresh = tokenManager.getRefreshToken();
        if (refresh == null || refresh.isEmpty()) {
            return tokenManager.getAccessToken();
        }

        try {
            OkHttpClient refreshClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .callTimeout(40, TimeUnit.SECONDS)
                    .build();
            String json = new Gson().toJson(new TokenRefreshRequest(refresh));
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(BASE_URL + "auth/refresh/")
                    .post(RequestBody.create(json, MediaType.get("application/json; charset=utf-8")))
                    .build();
            try (okhttp3.Response response = refreshClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return null;
                }
                TokenRefreshResponse body = new Gson().fromJson(response.body().string(), TokenRefreshResponse.class);
                if (body == null || body.access == null || body.access.isEmpty()) {
                    return null;
                }
                tokenManager.saveAccessToken(body.access);
                return body.access;
            }
        } catch (IOException ignored) {
            return tokenManager.getAccessToken();
        }
    }

    private static int responseCount(okhttp3.Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
