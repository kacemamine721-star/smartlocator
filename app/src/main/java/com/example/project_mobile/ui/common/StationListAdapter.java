package com.example.project_mobile.ui.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.R;
import com.example.project_mobile.data.ChargingStation;
import com.example.project_mobile.ui.details.StationDetailsActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.StationViewHolder> {

    private List<ChargingStation> stations;

    public StationListAdapter(List<ChargingStation> stations) {
        this.stations = new java.util.ArrayList<>(stations);
    }

    public void updateStations(List<ChargingStation> newStations) {
        this.stations.clear();
        this.stations.addAll(newStations);
        notifyDataSetChanged();
    }

    public ChargingStation getStationAt(int position) {
        return stations.get(position);
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        MaterialCardView cardView = new MaterialCardView(context);
        RecyclerView.LayoutParams cardParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(context, 14);
        cardView.setLayoutParams(cardParams);
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.zid_surface));
        cardView.setRadius(dp(context, 14));
        cardView.setStrokeColor(ContextCompat.getColor(context, R.color.zid_border));
        cardView.setStrokeWidth(dp(context, 1));

        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int cardPadding = dp(context, 18);
        row.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);

        LinearLayout infoColumn = new LinearLayout(context);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoColumn.setLayoutParams(infoParams);
        infoColumn.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(context);
        name.setTextColor(ContextCompat.getColor(context, R.color.zid_text_primary));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);

        TextView meta = new TextView(context);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        metaParams.topMargin = dp(context, 6);
        meta.setLayoutParams(metaParams);
        meta.setTextColor(ContextCompat.getColor(context, R.color.zid_text_secondary));
        meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        TextView connectors = new TextView(context);
        LinearLayout.LayoutParams connectorsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        connectorsParams.topMargin = dp(context, 6);
        connectors.setLayoutParams(connectorsParams);
        connectors.setTextColor(ContextCompat.getColor(context, R.color.zid_teal));
        connectors.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

        infoColumn.addView(name);
        infoColumn.addView(meta);
        infoColumn.addView(connectors);

        LinearLayout actionsColumn = new LinearLayout(context);
        actionsColumn.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        actionsColumn.setGravity(Gravity.END);
        actionsColumn.setOrientation(LinearLayout.VERTICAL);

        TextView status = new TextView(context);
        status.setTextColor(ContextCompat.getColor(context, R.color.zid_black));
        status.setTypeface(status.getTypeface(), android.graphics.Typeface.BOLD);
        int statusHorizontal = dp(context, 12);
        int statusVertical = dp(context, 8);
        status.setPadding(statusHorizontal, statusVertical, statusHorizontal, statusVertical);

        ImageButton openMapButton = new ImageButton(context);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(dp(context, 40), dp(context, 40));
        buttonParams.topMargin = dp(context, 12);
        buttonParams.gravity = Gravity.END;
        openMapButton.setLayoutParams(buttonParams);
        openMapButton.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_map_control));
        openMapButton.setImageResource(android.R.drawable.ic_menu_directions);
        openMapButton.setColorFilter(ContextCompat.getColor(context, R.color.zid_text_primary));
        openMapButton.setContentDescription(context.getString(R.string.open_on_map));

        actionsColumn.addView(status);
        actionsColumn.addView(openMapButton);

        row.addView(infoColumn);
        row.addView(actionsColumn);
        cardView.addView(row);

        return new StationViewHolder(cardView, name, meta, connectors, status, openMapButton);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        ChargingStation station = stations.get(position);
        holder.name.setText(station.name);
        String ratingText = station.ratingCount > 0 ? String.format(java.util.Locale.US, "★ %.1f (%d) • ", station.averageRating, station.ratingCount) : "New • ";
        holder.meta.setText(ratingText + station.city + " • " + station.distance + " • " + station.power);
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
        int colorRes = R.color.zid_text_tertiary;
        if ("Available".equals(status)) {
            colorRes = R.color.zid_teal;
        } else if ("Busy".equals(status)) {
            colorRes = R.color.zid_gold;
        } else if ("Offline".equals(status)) {
            colorRes = R.color.zid_text_tertiary;
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

        StationViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView meta,
                TextView connectors,
                TextView status,
                ImageButton openMapButton
        ) {
            super(itemView);
            this.name = name;
            this.meta = meta;
            this.connectors = connectors;
            this.status = status;
            this.openMapButton = openMapButton;
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()
        ));
    }
}
