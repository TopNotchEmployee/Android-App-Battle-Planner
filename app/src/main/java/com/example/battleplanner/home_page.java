package com.example.battleplanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class home_page extends AppCompatActivity {

    private String userId; // User ID of the current user
    private EditText editTextEmailAddress; // EditText for email input
    private EditText editTextPassword; // EditText for password input
    private ScrollView fieldContainer;// Container for fields
    private UserOnlineStatusManager onlineStatusManager; // User online status manager
    private TextView textViewFieldName;
    private ImageView imageViewBusiness;
    private List<View> fieldViewsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        onlineStatusManager = new UserOnlineStatusManager(); // Initialize the online status manager

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            userId = currentUser.getUid();
            String userName = currentUser.getDisplayName();
            Log.d("Username", "Username " + userName);
        }



        LinearLayout fieldContainer = new LinearLayout(home_page.this);
        fieldContainer.setOrientation(LinearLayout.VERTICAL);



        fieldViewsList = new ArrayList<>();



        // Fetch data entries from BusinessDataRetriever
        BusinessDataRetriever.fetchDataEntries(new BusinessDataRetriever.DataEntryListener() {
            @Override
            public void onDataEntryLoaded(List<BusinessDataRetriever.DataEntry> dataEntries) {


                // Create views for each data entry and add them to the fieldContainer
                for (BusinessDataRetriever.DataEntry dataEntry : dataEntries) {
                    View fieldView = LayoutInflater.from(home_page.this).inflate(R.layout.field_item, fieldContainer, false);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    fieldView.setLayoutParams(layoutParams);

                    TextView textViewFieldName = fieldView.findViewById(R.id.textViewFieldName);
                    ImageView imageViewBusiness = fieldView.findViewById(R.id.imageViewBusiness);
                    Button buttonView = fieldView.findViewById(R.id.buttonView);

                    textViewFieldName.setText(dataEntry.getFieldName());
                    Picasso.get().load(dataEntry.fieldImageUrl).into(imageViewBusiness);


                    buttonView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Implement what you want to do when the "View" button is clicked for a specific field.
                            // For example, you can open a new activity to show more details about the field.

                            // Get the field name and image URL from the clicked item
                            String fieldName = dataEntry.getFieldName();
                            String imageUrl = dataEntry.getFieldImageUrl();

                            // Create an intent to start the business_field_details_page activity
                            Intent intent = new Intent(home_page.this, business_field_details_page.class);

                            // Pass necessary data to the business_field_details_page activity using intent extras
                            intent.putExtra("fieldName", fieldName);
                            intent.putExtra("imageUrl", imageUrl);

                            // Start the business_field_details_page activity
                            startActivity(intent);
                        }
                    });

                    fieldContainer.addView(fieldView); // Add the fieldView to the fieldContainer
                    fieldViewsList.add(fieldView); // Optional: Keep a reference to the views for later use if needed
                }
                // Add the fieldContainer to the ScrollView
                ScrollView scrollView = findViewById(R.id.fieldContainer);
                scrollView.addView(fieldContainer);
            }

            @Override
            public void onDataEntryError(DatabaseError databaseError) {
                // Handle the error if loading data entries fails
            }
        });

        Button calendarButton = findViewById(R.id.buttonCalender);
        Button eventButton = findViewById(R.id.buttonEvents);
        Button settingsButton = findViewById(R.id.buttonCog);

        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(home_page.this, calender_page.class);
            startActivity(intent);
        });

        eventButton.setOnClickListener(v -> {
            Intent intent = new Intent(home_page.this, event_page.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(home_page.this, user_customization.class);
            startActivity(intent);
        });

        Button btnGoFriends = findViewById(R.id.buttonFriends);

        btnGoFriends.setOnClickListener(v -> {
            Intent intent = new Intent(home_page.this, friends_page.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        if (savedInstanceState == null) {
            boolean rememberMe = getIntent().getBooleanExtra("remember_me", false);
            Log.d("RememberMe", "Value: " + rememberMe);
            String email = getIntent().getStringExtra("email");
            String password = getIntent().getStringExtra("password");

            Log.d("Email", "Value: " + email);
            Log.d("Password", "Value: " + password);

            if (rememberMe) {
                email = getIntent().getStringExtra("email");
                password = getIntent().getStringExtra("password");

                editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
                editTextPassword = findViewById(R.id.editTextPassword);

                if (email != null && editTextEmailAddress != null) {
                    editTextEmailAddress.setText(email);
                }

                if (password != null && editTextPassword != null) {
                    editTextPassword.setText(password);
                }
            } else {
                editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
                editTextPassword = findViewById(R.id.editTextPassword);

                if (editTextEmailAddress != null) {
                    editTextEmailAddress.setText("");
                }

                if (editTextPassword != null) {
                    editTextPassword.setText("");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onlineStatusManager.setOnlineStatus(true);
        Log.d("Resume", "Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        onlineStatusManager.setOnlineStatus(false);
        Log.d("Pause", "Pause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        onlineStatusManager.setOnlineStatus(true);
        Log.d("Start", "Start");
    }

    @Override
    public void onBackPressed() {
        boolean rememberMe = getIntent().getBooleanExtra("remember_me", false);
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (rememberMe) {
                        Intent intent = new Intent(home_page.this, login_page.class);
                        intent.putExtra("remember_me", true);
                        if (editTextEmailAddress != null) {
                            intent.putExtra("email", editTextEmailAddress.getText().toString());
                        }
                        if (editTextPassword != null) {
                            intent.putExtra("password", editTextPassword.getText().toString());
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(home_page.this, login_page.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }



    public class Field {
        private String field_name;
        private String imageUrl;

        public Field(String field_name, String imageUrl) {
            this.field_name = field_name;
            this.imageUrl = imageUrl;
        }

        public String getField_Name() {
            return field_name;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
