package com.example.project_mobile.ui.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.R;
import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.ui.details.StationDetailsActivity;

import java.util.List;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.StationViewHolder> {

    private final List<ChargingStation> stations;

    public StationListAdapter(List<ChargingStation> stations) {
        this.stations = stations;
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station_card, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        ChargingStation station = stations.get(position);
        holder.name.setText(station.name);
        holder.meta.setText(station.city + " • " + station.distance + " • " + station.power);
        holder.connectors.setText(android.text.TextUtils.join(" • ", station.connectors));
        holder.status.setText(station.status);
        holder.status.setBackground(buildStatusBackground(holder.itemView.getContext(), station.status));
        holder.openMapButton.setOnClickListener(v -> openDetails(v.getContext(), station));
        holder.itemView.setOnClickListener(v -> openDetails(v.getContext(), station));
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    private GradientDrawable buildStatusBackground(Context context, String status) {
        int colorRes = R.color.unknown_gray;
        if ("Available".equals(status)) {
            colorRes = R.color.available_green;
        } else if ("Busy".equals(status)) {
            colorRes = R.color.busy_orange;
        } else if ("Offline".equals(status)) {
            colorRes = R.color.offline_red;
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(context, colorRes));
        drawable.setCornerRadius(100f);
        return drawable;
    }

    private void openDetails(Context context, ChargingStation station) {
        Intent intent = StationDetailsActivity.createIntent(context, station);
        context.startActivity(intent);
    }

    static class StationViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final TextView connectors;
        final TextView status;
        final ImageButton openMapButton;

        StationViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.station_name);
            meta = itemView.findViewById(R.id.station_meta);
            connectors = itemView.findViewById(R.id.station_connectors);
            status = itemView.findViewById(R.id.station_status);
            openMapButton = itemView.findViewById(R.id.station_open_button);
        }
    }
}
