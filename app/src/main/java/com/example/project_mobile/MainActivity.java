package com.example.project_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project_mobile.data.TokenManager;
import com.example.project_mobile.ui.auth.WelcomeActivity;
import com.example.project_mobile.ui.favorites.FavoritesFragment;
import com.example.project_mobile.ui.history.HistoryFragment;
import com.example.project_mobile.ui.map.MapFragment;
import com.example.project_mobile.ui.profile.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private int selectedItemId = R.id.navigation_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new TokenManager(this).hasToken()) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            selectedItemId = savedInstanceState.getInt("selected_item", R.id.navigation_map);
        }

        NavigationBarView bottomNavigation = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int horizontalMargin = dpToPx(20);
            int bottomMargin = dpToPx(16);
            layoutParams.leftMargin = horizontalMargin;
            layoutParams.rightMargin = horizontalMargin;
            layoutParams.bottomMargin = bottomMargin + systemBars.bottom;
            view.setLayoutParams(layoutParams);
            return windowInsets;
        });
        bottomNavigation.setOnItemSelectedListener(item -> {
            selectedItemId = item.getItemId();
            switchToDestination(selectedItemId);
            return true;
        });

        bottomNavigation.setSelectedItemId(selectedItemId);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("routing_station_id")) {
            String stationId = intent.getStringExtra("routing_station_id");
            switchToDestination(R.id.navigation_map);
            findViewById(R.id.bottom_navigation).post(() -> {
                ((NavigationBarView) findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.navigation_map);
                getSupportFragmentManager().executePendingTransactions();
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (f instanceof MapFragment) {
                    ((MapFragment) f).selectStationAndRoute(stationId);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_item", selectedItemId);
    }

    private void switchToDestination(int itemId) {
        Fragment fragment;
        if (itemId == R.id.navigation_favorites) {
            fragment = new FavoritesFragment();
        } else if (itemId == R.id.navigation_history) {
            fragment = new HistoryFragment();
        } else if (itemId == R.id.navigation_profile) {
            fragment = new ProfileFragment();
        } else {
            fragment = new MapFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private int dpToPx(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        ));
    }
}
