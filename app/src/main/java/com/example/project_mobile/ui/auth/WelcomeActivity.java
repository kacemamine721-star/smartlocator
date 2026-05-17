package com.example.project_mobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_mobile.MainActivity;
import com.example.project_mobile.R;
import com.example.project_mobile.data.TokenManager;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (new TokenManager(this).hasToken()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        android.widget.VideoView videoView = findViewById(R.id.welcome_video_view);
        android.net.Uri uri = android.net.Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.welcome_video);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
        });
        videoView.start();


        findViewById(R.id.btn_welcome_login).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.btn_welcome_register).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
