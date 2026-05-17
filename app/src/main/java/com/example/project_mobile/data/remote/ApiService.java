package com.example.project_mobile.data.remote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {
    @GET("stations/")
    Call<List<StationDto>> getStations();

    @POST("auth/login/")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register/")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("contributions/")
    Call<ContributionResponse> submitContribution(@Body ContributionRequest request);

    @POST("alerts/")
    Call<Void> submitAlert(@Body AlertRequest request);

    @GET("vehicles/")
    Call<List<EVVehicle>> getVehicles();

    @GET("users/me/")
    Call<UserMeResponse> getUserMe();

    @PUT("users/me/")
    Call<UserMeResponse> updateProfile(@Body UpdateProfileRequest request);
}
