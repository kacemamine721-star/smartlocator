package com.example.project_mobile.data.remote;

import java.io.Serializable;

public class EVVehicle implements Serializable {
    public int id;
    public String vehicle_id;
    public String brand;
    public String model_name;
    public String segment;
    public Float battery_capacity_kwh;
    public Float usable_capacity_kwh;
    public Integer range_wltp_km;
    public Float ac_max_power_kw;
    public String ac_connector_type;
    public Integer ac_phases;
    public Float dc_max_power_kw;
    public String dc_connector_type;
    public Integer km_per_hour_ac;
    public Integer km_per_hour_dc;
    public Float full_charge_time_ac_hours;
    public Integer full_charge_time_dc_minutes;
    public String image;
}
