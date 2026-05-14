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
import com.example.project_mobile.data.remote.RegisterRequest;
import com.example.project_mobile.data.remote.RegisterResponse;
import com.example.project_mobile.data.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton registerButton;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        tokenManager = new TokenManager(this);
        usernameInput = findViewById(R.id.register_username_input);
        emailInput = findViewById(R.id.register_email_input);
        passwordInput = findViewById(R.id.register_password_input);
        registerButton = findViewById(R.id.btn_register);

        registerButton.setOnClickListener(v -> register());
        findViewById(R.id.btn_open_login).setOnClickListener(v -> finish());
    }

    private void register() {
        String username = textOf(usernameInput);
        String email = textOf(emailInput);
        String password = textOf(passwordInput);
        if (username.isEmpty() || password.length() < 8) {
            Toast.makeText(this, "Use a username and an 8+ character password", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        RetrofitClient.getApiService(this)
                .register(new RegisterRequest(username, email, password))
                .enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                        if (response.isSuccessful()) {
                            loginAfterRegister(username, password);
                        } else {
                            setLoading(false);
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage(response), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginAfterRegister(String username, String password) {
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
                            Toast.makeText(RegisterActivity.this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        registerButton.setEnabled(!loading);
        registerButton.setText(loading ? "Creating..." : "Create Account");
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
