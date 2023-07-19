package com.example.battleplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.util.List;

public class business_field_details_page extends AppCompatActivity {

    private TextView fieldNameTextView;
    private TextView emailTextView;
    private TextView contactNumberTextView;
    private TextView googleMapsLinkTextView;
    private TextView whatsappLinkTextView;
    private ImageView imageViewFieldImage;
    private String currentUserId;
    private Button btnAddEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_field_details_page);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // The user is logged in, so you can get the userID
            currentUserId = currentUser.getUid();
            Log.d("logged user", currentUserId);
        }

        imageViewFieldImage = findViewById(R.id.imageViewFieldImage);
        fieldNameTextView = findViewById(R.id.fieldName);
        emailTextView = findViewById(R.id.email);
        contactNumberTextView = findViewById(R.id.contactNumber);
        googleMapsLinkTextView = findViewById(R.id.googleMapsLink);
        whatsappLinkTextView = findViewById(R.id.whatsappLink);
        Button btnViewEvents = findViewById(R.id.btnViewEvents);
        btnAddEvent = findViewById(R.id.btnAddEvent);

        retrieveDataEntries();

        // You can add click listeners to buttons as follows:
        btnViewEvents.setOnClickListener(view -> {
            Intent intent = new Intent(this, event_page.class);
            startActivity(intent);
        });

        btnAddEvent.setOnClickListener(view -> {
            Intent intent = new Intent(this, add_event.class);
            startActivity(intent);
        });
    }

    private void retrieveDataEntries() {
        BusinessDataRetriever.fetchDataEntries(new BusinessDataRetriever.DataEntryListener() {
            @Override
            public void onDataEntryLoaded(List<BusinessDataRetriever.DataEntry> dataEntries) {
                // Process the retrieved dataEntries here
                for (BusinessDataRetriever.DataEntry dataEntry : dataEntries) {
                    String fieldName = dataEntry.getFieldName();
                    String email = dataEntry.getEmail();
                    String link = dataEntry.getLink();
                    String location = dataEntry.getLocation();
                    String number = dataEntry.getNumber();
                    String whatsappGroup = dataEntry.getWhatsappGroup();
                    String fieldImageUrl = dataEntry.getFieldImageUrl();

                    RequestOptions requestOptions = new RequestOptions()
                            .error(R.drawable.login_bg) // Error image if loading fails
                            .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache the image

                    Glide.with(business_field_details_page.this)
                            .load(fieldImageUrl)
                            .apply(requestOptions)
                            .into(imageViewFieldImage);

                    // You can now use this information to populate the views in your activity
                    fieldNameTextView.setText("Field Name: " + fieldName);
                    emailTextView.setText("Email: " + email);
                    contactNumberTextView.setText("Contact Number: " + number);
                    googleMapsLinkTextView.setText("Google Maps: " + location);
                    whatsappLinkTextView.setText("Whatsapp Group: " + whatsappGroup);

                    String ownerId = dataEntry.getOwnerId();
                    boolean isCurrentUserOwner = currentUserId.equals(ownerId);

                    if (isCurrentUserOwner) {
                        // Show the "Add Event" button for the owner
                        btnAddEvent.setVisibility(View.VISIBLE);
                    } else {
                        // Hide the "Add Event" button for other users
                        btnAddEvent.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onDataEntryError(DatabaseError databaseError) {
                String errorMessage = databaseError.getMessage();
                // Handle the error if necessary
            }
        });
    }
}
