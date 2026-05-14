package com.example.project_mobile.data.remote;

public class LoginRequest {
    public final String username;
    public final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
