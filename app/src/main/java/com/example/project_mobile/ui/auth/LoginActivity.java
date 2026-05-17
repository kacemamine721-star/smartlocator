package com.example.project_mobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_mobile.MainActivity;
import com.example.project_mobile.R;
import com.example.project_mobile.data.TokenManager;
import com.example.project_mobile.data.remote.AuthResponse;
import com.example.project_mobile.data.remote.LoginRequest;
import com.example.project_mobile.data.remote.RetrofitClient;
import com.example.project_mobile.data.remote.UserMeResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);
        usernameInput = findViewById(R.id.login_username_input);
        passwordInput = findViewById(R.id.login_password_input);
        loginButton = findViewById(R.id.btn_login);

        loginButton.setOnClickListener(v -> login());
        findViewById(R.id.btn_open_register).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = textOf(usernameInput);
        String password = textOf(passwordInput);
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter your username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        RetrofitClient.getApiService(this)
                .login(new LoginRequest(username, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null) {
                            tokenManager.saveTokens(body.access, body.refresh);
                            tokenManager.saveUserName(username);
                            openMain();
                        } else {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage(response), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMain() {
        RetrofitClient.getApiService(this).getUserMe().enqueue(new Callback<UserMeResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserMeResponse> call, @NonNull Response<UserMeResponse> response) {
                Intent intent;
                if (response.isSuccessful() && response.body() != null 
                    && response.body().profile != null 
                    && response.body().profile.vehicle != null) {
                    // User already has a vehicle, go straight to MainActivity
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                } else {
                    // User needs to select a vehicle
                    intent = new Intent(LoginActivity.this, EVSelectionActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(@NonNull Call<UserMeResponse> call, @NonNull Throwable t) {
                // If the check fails, fallback to MainActivity just in case
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "Signing in..." : "Log In");
    }

    private String errorMessage(Response<?> response) {
        if (response.errorBody() == null) {
            return "HTTP " + response.code();
        }
        try {
            return response.errorBody().string();
        } catch (IOException e) {
            return "HTTP " + response.code();
        }
    }
}
