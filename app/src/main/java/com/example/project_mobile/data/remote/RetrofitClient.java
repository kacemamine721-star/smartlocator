package com.example.project_mobile.data.remote;

import android.content.Context;

import com.example.project_mobile.data.TokenManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = new TokenManager(context.getApplicationContext());
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String token = tokenManager.getAccessToken();
                        okhttp3.Request request = chain.request();
                        if (token != null && !token.isEmpty()) {
                            android.util.Log.d("RetrofitClient", "Attaching token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                            request = request.newBuilder()
                                    .addHeader("Authorization", "Bearer " + token)
                                    .build();
                        } else {
                            android.util.Log.w("RetrofitClient", "No token found in TokenManager!");
                        }
                        return chain.proceed(request);
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
}
