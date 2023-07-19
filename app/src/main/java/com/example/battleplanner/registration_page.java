package com.example.battleplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class registration_page extends AppCompatActivity {
    private DatabaseReference usersRef;
    private EditText editTextUsername;
    private EditText editTextName;
    private EditText editTextBirthday;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        // Initialize Firebase database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/");
        usersRef = database.getReference("accounts");

        // Find the EditText views by their IDs
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextName = findViewById(R.id.editTextName);
        editTextBirthday = findViewById(R.id.editTextBirthday);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        // Find the registerButton and set a click listener to call the register() method when clicked
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> register());
    }

    private void register() {
        // Retrieve the entered values from the EditText views
        String username = editTextUsername.getText().toString();
        String name = editTextName.getText().toString();
        String birthday = editTextBirthday.getText().toString().trim(); // Remove leading/trailing whitespaces
        String email = editTextEmail.getText().toString().toLowerCase();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        // Perform validation checks on the entered values

        // Check if any field is empty
        if (username.isEmpty() || name.isEmpty() || birthday.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the password meets the complexity requirements
        if (!isPasswordComplex(password)) {
            Toast.makeText(this, "Password should have at least 8 characters and contain a combination of letters, numbers, and special characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the password and confirm password fields match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the email or username already exist in the database
        Query usernameQuery = usersRef.orderByChild("username").equalTo(username);
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username already exists in the database
                    Toast.makeText(registration_page.this, "Username already exists", Toast.LENGTH_SHORT).show();
                } else {
                    // Username is unique, check email uniqueness
                    Query emailQuery = usersRef.orderByChild("email").equalTo(email);
                    emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Email already exists in the database
                                Toast.makeText(registration_page.this, "Email already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                // Email is also unique, proceed with checking birthday format
                                if (!isValidDateFormat(birthday)) {
                                    // Invalid birthday format
                                    Toast.makeText(registration_page.this, "Invalid birthday format. Please enter in YYYY-MM-DD format.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Birthday format is valid, create user in the authentication system
                                    createUserInAuthentication(email, password, username, name, birthday);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Error occurred while checking email uniqueness
                            Toast.makeText(registration_page.this, "Failed to check email uniqueness", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error occurred while checking username uniqueness
                Toast.makeText(registration_page.this, "Failed to check username uniqueness", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Add TextWatcher to automatically insert hyphen after the first 4 characters for the year
        editTextBirthday.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed during text changed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Add hyphen after the first 4 characters for the year
                if (s.length() == 4 && s.charAt(3) != '-') {
                    s.insert(4, "-");
                }
                // Add hyphen after the first 7 characters for the month
                else if (s.length() == 7 && s.charAt(6) != '-') {
                    s.insert(7, "-");
                }
                // Delete extra characters if the input exceeds 10 characters
                else if (s.length() > 10) {
                    s.delete(10, s.length());
                }

                // Check if the month exceeds 12
                if (s.length() >= 6) {
                    String monthString = s.subSequence(5, Math.min(7, s.length())).toString().replace("-", "");
                    if (!monthString.isEmpty()) {
                        int month = Integer.parseInt(monthString);
                        if (month > 12) {
                            // Display a toast message for an invalid month
                            Toast.makeText(getApplicationContext(), "Invalid month", Toast.LENGTH_SHORT).show();
                            // Delete the invalid month
                            s.delete(6, 7);
                        }
                    }
                }

                // Check if the day exceeds 31
                if (s.length() >= 9) {
                    String dayString = s.subSequence(8, Math.min(10, s.length())).toString().replace("-", "");
                    if (!dayString.isEmpty()) {
                        int day = Integer.parseInt(dayString);
                        if (day > 31) {
                            // Display a toast message for an invalid day
                            Toast.makeText(getApplicationContext(), "Invalid day", Toast.LENGTH_SHORT).show();
                            // Delete the invalid day
                            s.delete(9, 10);
                        }
                    }
                }
            }
        });
    }
    private boolean isValidDateFormat(String date) {
        // Check if the date is in the format YYYY-MM-DD
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }


    public void openLoginPage() {
        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Display a toast message when the verification email is sent successfully
                        Toast.makeText(registration_page.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        // Display a toast message when sending the verification email fails
                        Toast.makeText(registration_page.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                    }
                    // Open the login page after email verification
                    Intent intent = new Intent(this, login_page.class);
                    startActivity(intent);
                });
    }

    public static class User {
        private String username;
        private String name;
        private String birthday;
        private String email;

        public User() {
            // Default constructor required for Firebase
        }

        public User(String username, String name, String birthday, String email) {
            this.username = username;
            this.name = name;
            this.birthday = birthday;
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }

        public String getBirthday() {
            return birthday;
        }

        public String getEmail() {
            return email;
        }
    }

    private boolean isPasswordComplex(String password) {
        // Check minimum length
        if (password.length() < 8) {
            return false;
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Check for at least one letter
        if (!password.matches(".*[a-zA-Z].*")) {
            return false;
        }

        // Check for at least one special character
        return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }

    private void createUserInAuthentication(String email, String password, String username, String name, String birthday) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User creation successful, save additional details to the Realtime Database
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        assert firebaseUser != null;
                        String userId = firebaseUser.getUid();

                        // Create a new user object
                        User user = new User(username, name, birthday, email);

                        // Save the user object to the Realtime Database under the generated key
                        usersRef.child(userId).setValue(user);

                        Toast.makeText(registration_page.this, "Registration successful", Toast.LENGTH_SHORT).show();

                        openLoginPage();
                    } else {
                        // User creation failed
                        Toast.makeText(registration_page.this, "Failed to create user: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
