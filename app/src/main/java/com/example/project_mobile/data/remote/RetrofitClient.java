package com.example.project_mobile.data.remote;

import android.content.Context;

import com.google.gson.Gson;
import com.example.project_mobile.data.TokenManager;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    // Senior Dev Optimization: Using BuildConfig to switch between local and production URLs automatically
    private static final String BASE_URL = com.example.project_mobile.BuildConfig.BASE_URL;
    private static Retrofit retrofit = null;

    public static ApiService getApiService(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = new TokenManager(context.getApplicationContext());
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        String token = tokenManager.getAccessToken();
                        okhttp3.Request request = chain.request();
                        if (!isPublicRequest(request) && tokenManager.isAccessTokenExpired()) {
                            token = refreshAccessToken(tokenManager);
                        }
                        if (token != null && !token.isEmpty() && !isPublicRequest(request)) {
                            android.util.Log.d("RetrofitClient", "Attaching token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                            request = request.newBuilder()
                                    .addHeader("Authorization", "Bearer " + token)
                                    .build();
                        } else {
                            android.util.Log.w("RetrofitClient", "No token found in TokenManager!");
                        }
                        return chain.proceed(request);
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
        }
        return retrofit.create(ApiService.class);
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
