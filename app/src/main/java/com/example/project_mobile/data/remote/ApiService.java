package com.example.project_mobile.data.remote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("stations/")
    Call<List<StationDto>> getStations();
}
