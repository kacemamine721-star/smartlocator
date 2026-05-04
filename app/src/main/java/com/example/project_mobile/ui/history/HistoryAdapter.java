package com.example.project_mobile.ui.history;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.R;
import com.example.project_mobile.data.local.HistoryEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryEntity> sessions = new ArrayList<>();
    private final OnRateClickListener rateClickListener;

    public interface OnRateClickListener {
        void onRateClick(HistoryEntity session);
    }

    public HistoryAdapter(OnRateClickListener rateClickListener) {
        this.rateClickListener = rateClickListener;
    }

    public void updateSessions(List<HistoryEntity> newSessions) {
        sessions.clear();
        sessions.addAll(newSessions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntity session = sessions.get(position);
        
        holder.stationName.setText(session.stationName);
        
        String dateStr = DateFormat.format("MMM dd, yyyy", new Date(session.date)).toString();
        String detailStr = session.routeOnly 
            ? dateStr + " • Route only" 
            : dateStr + " • " + String.format("%.1f", session.kwhCharged) + " kWh";
            
        holder.dateKwh.setText(detailStr);
        
        if (session.routeOnly) {
            holder.icon.setImageResource(android.R.drawable.ic_menu_directions);
        } else {
            holder.icon.setImageResource(android.R.drawable.ic_lock_idle_charging);
        }

        holder.rateButton.setOnClickListener(v -> {
            if (rateClickListener != null) {
                rateClickListener.onRateClick(session);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView stationName;
        final TextView dateKwh;
        final ImageView icon;
        final ImageButton rateButton;

        ViewHolder(View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.history_station_name);
            dateKwh = itemView.findViewById(R.id.history_date_kwh);
            icon = itemView.findViewById(R.id.history_icon);
            rateButton = itemView.findViewById(R.id.history_rate_button);
        }
    }
}
