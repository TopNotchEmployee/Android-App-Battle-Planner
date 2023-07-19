package com.example.battleplanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class login_page extends AppCompatActivity {
    private DatabaseReference onlineStatusRef;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private int failedLoginAttempts = 0;
    CheckBox rememberMeCheckbox;
    private EditText editTextEmail;
    private EditText editTextPassword;

    protected void onResume() {
        super.onResume();

        // Retrieve the remember_me, email, and password values from SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean rememberMe = preferences.getBoolean("remember_me", false);
        String email = preferences.getString("email", "");
        String password = preferences.getString("password", "");

        // Set the values in the email and password fields if rememberMe is true
        if (rememberMe) {
            editTextEmail.setText(email);
            editTextPassword.setText(password);
            rememberMeCheckbox.setChecked(true);
        } else {
            // Reset the email and password fields to empty strings
            editTextEmail.setText("");
            editTextPassword.setText("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Check if a user is logged in
        if (currentUser != null) {
            // Set the online status of the user to false
            onlineStatusRef = database.getReference("users/" + currentUser.getUid() + "/online");
            onlineStatusRef.setValue(false);
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        // Retrieve the remember_me, email, and password values from the intent
        boolean rememberMe = getIntent().getBooleanExtra("remember_me", false);
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");

        // Set the values in the email and password fields if rememberMe is true
        editTextEmail = findViewById(R.id.editTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextPassword);

        if (rememberMe) {
            editTextEmail.setText(email);
            editTextPassword.setText(password);
        }

        // Get reference to rememberMeCheckbox after setting the content view
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        // Set the checked state of the rememberMeCheckbox based on the rememberMe value
        rememberMeCheckbox.setChecked(rememberMe);

        // Set click listeners for login and register buttons
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> login());

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> openRegistrationPage());

        // Set onlineStatusRef to the appropriate database reference
        if (currentUser != null) {
            onlineStatusRef = database.getReference("users/" + currentUser.getUid() + "/online");
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Exit the app
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    // Method to handle the login process
    public void login() {
        // Retrieve entered email and password
        String enteredEmail = editTextEmail.getText().toString();
        String enteredPassword = editTextPassword.getText().toString();

        // Check if email field is empty
        if (enteredEmail.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password field is empty
        if (enteredPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt to sign in with the entered email and password
        mAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Check if the user's email is verified
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Login successful and email is verified, proceed to the home page
                            boolean rememberMe = rememberMeCheckbox.isChecked();
                            openHomePage(rememberMe, enteredEmail, enteredPassword);

                            // Store the login details and remember me flag in SharedPreferences
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                            preferences.edit()
                                    .putBoolean("remember_me", rememberMe)
                                    .putString("email", enteredEmail)
                                    .putString("password", enteredPassword)
                                    .apply();

                            // Set online status immediately after login
                            String userId = user.getUid();
                            onlineStatusRef = database.getReference("users/" + userId + "/online");
                            onlineStatusRef.setValue(true); // Set online status to true
                        } else {
                            // User's email is not verified
                            Toast.makeText(getApplicationContext(), "Please verify your email first", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else {
                        // Handle login failure
                        failedLoginAttempts++;
                        if (failedLoginAttempts % 2 == 0) {
                            // After every 2nd failed attempt, show a password reset dialog
                            showResetPasswordDialog();
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidUserException invalidEmail) {
                                // Handle invalid email exception
                                Toast.makeText(getApplicationContext(), "No user found with this email", Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                                // Handle invalid password exception
                                Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                // Handle other exceptions
                                Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    // Reset password if login failed twice
    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Do you want to reset your password?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Reset password
            resetPassword();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // Continue with login attempts
            editTextEmail.setText("");
            editTextPassword.setText("");
        });
        builder.setCancelable(false);
        builder.show();
    }

    // Password reset email sent out
    private void resetPassword() {
        String email = editTextEmail.getText().toString();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Navigate to the registration page
    public void openRegistrationPage() {
        Intent intent = new Intent(this, registration_page.class);
        startActivity(intent);
    }

    // Navigate to the home page
    public void openHomePage(boolean rememberMe, String enteredEmail, String enteredPassword) {
        Intent intent = new Intent(login_page.this, home_page.class);

        // Retrieve the values from the EditText fields
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        // Check if "Remember Me" checkbox is selected
        CheckBox rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        rememberMe = rememberMeCheckbox.isChecked();

        // Pass the login details and remember me flag to the home page
        intent.putExtra("remember_me", rememberMe);
        intent.putExtra("email", email);
        intent.putExtra("password", password);

        startActivity(intent);
    }
}
