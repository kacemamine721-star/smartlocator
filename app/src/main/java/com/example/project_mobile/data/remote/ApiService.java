package com.example.project_mobile.data.remote;

import java.util.List;
import com.example.project_mobile.data.model.RouteResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {
    @GET("stations/")
    Call<List<StationDto>> getStations();

    @POST("auth/login/")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register/")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("auth/refresh/")
    Call<TokenRefreshResponse> refreshToken(@Body TokenRefreshRequest request);

    @POST("contributions/")
    Call<ContributionResponse> submitContribution(@Body ContributionRequest request);

    @POST("alerts/")
    Call<Void> submitAlert(@Body AlertRequest request);

    @GET("alerts/")
    Call<List<CommunityAlert>> getAlerts();

    @GET("vehicles/")
    Call<List<EVVehicle>> getVehicles();

    @GET("users/me/")
    Call<UserMeResponse> getUserMe();

    @PUT("users/me/")
    Call<UserMeResponse> updateProfile(@Body UpdateProfileRequest request);

    @POST("stations/{id}/check_in/")
    Call<CheckInResponse> checkInStation(@retrofit2.http.Path("id") int id, @Body CheckInRequest request);

    @POST("stations/{id}/flag_as_broken/")
    Call<Void> flagStationAsBroken(@retrofit2.http.Path("id") int id);

    @POST("ratings/")
    Call<RatingResponse> submitRating(@Body RatingRequest request);

    @GET("route/")
    Call<RouteResponse> getRoute(
            @Query("from_lat") double fromLat,
            @Query("from_lng") double fromLng,
            @Query("to_lat") double toLat,
            @Query("to_lng") double toLng
    );

    @POST("history/")
    Call<Void> saveHistorySession(@Body HistorySessionRequest request);
}
