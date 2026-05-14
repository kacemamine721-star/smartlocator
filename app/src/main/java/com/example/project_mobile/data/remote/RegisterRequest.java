package com.example.project_mobile.data.remote;

public class RegisterRequest {
    public final String username;
    public final String email;
    public final String password;

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
