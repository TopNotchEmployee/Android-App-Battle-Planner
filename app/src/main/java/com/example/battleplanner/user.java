package com.example.battleplanner;

public class user {
    private String email;       // Represents the email address of the user
    private String password;    // Represents the password of the user

    public user() {
        // Default constructor required for Firebase
    }

    public user(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
