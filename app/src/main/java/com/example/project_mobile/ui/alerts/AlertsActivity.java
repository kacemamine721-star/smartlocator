package com.example.project_mobile.ui.alerts;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_mobile.R;
import com.example.project_mobile.data.MockStationRepository;

public class AlertsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alerts);

        findViewById(R.id.alerts_back).setOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.alerts_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_history_row, R.id.history_text, MockStationRepository.getAlerts());
        listView.setAdapter(adapter);
    }
}
