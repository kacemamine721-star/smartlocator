package com.example.project_mobile.data.remote;

public class UpdateProfileRequest {
    public Integer vehicle_id;
    public Integer current_soc;

    public UpdateProfileRequest(Integer vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public UpdateProfileRequest(Integer vehicle_id, Integer current_soc) {
        this.vehicle_id = vehicle_id;
        this.current_soc = current_soc;
    }
}
