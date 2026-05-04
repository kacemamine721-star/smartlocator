package com.example.project_mobile.ui.alerts;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.project_mobile.R;
import com.example.project_mobile.data.MockStationRepository;

public class AlertsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(buildContentView());

        ImageButton backButton = findViewById(android.R.id.button1);
        backButton.setOnClickListener(v -> finish());

        ListView listView = findViewById(android.R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, MockStationRepository.getAlerts()) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                int padding = dp(18);
                textView.setPadding(padding, padding, padding, padding);
                textView.setTextColor(ContextCompat.getColor(AlertsActivity.this, R.color.white));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                textView.setBackground(ContextCompat.getDrawable(AlertsActivity.this, R.drawable.bg_panel_surface));
                textView.setLayoutParams(new android.widget.AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                return textView;
            }
        };
        listView.setAdapter(adapter);
    }

    private LinearLayout buildContentView() {
        LinearLayout root = new LinearLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.slate_950));
        root.setOrientation(LinearLayout.VERTICAL);
        int pagePadding = dp(20);
        root.setPadding(pagePadding, pagePadding, pagePadding, pagePadding);

        ImageButton backButton = new ImageButton(this);
        backButton.setId(android.R.id.button1);
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        backButton.setLayoutParams(backParams);
        backButton.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_map_control));
        backButton.setImageResource(android.R.drawable.ic_media_previous);
        backButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        backButton.setContentDescription(getString(R.string.alerts));

        TextView title = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(18);
        title.setLayoutParams(titleParams);
        title.setText(R.string.alerts);
        title.setTextColor(ContextCompat.getColor(this, R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        ListView listView = new ListView(this);
        listView.setId(android.R.id.list);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        listParams.topMargin = dp(18);
        listView.setLayoutParams(listParams);
        listView.setDivider(null);
        listView.setDividerHeight(dp(12));
        listView.setCacheColorHint(ContextCompat.getColor(this, android.R.color.transparent));

        root.addView(backButton);
        root.addView(title);
        root.addView(listView);
        return root;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        ));
    }
}
