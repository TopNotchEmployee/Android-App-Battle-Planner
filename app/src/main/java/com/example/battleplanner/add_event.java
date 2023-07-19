package com.example.battleplanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class add_event extends AppCompatActivity {

    private DatabaseReference eventRef;
    private EditText editTextFieldName;
    private EditText editTextEventName;
    private EditText editTextTicketNum;
    private EditText editTextTicketPrice;
    private EditText editTextYear;
    private EditText editTextMonth;
    private EditText editTextDay;
    private EditText editTextDescription;
    private FirebaseStorage storage;
    private StorageReference imageRef;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/");
        eventRef = database.getReference("events"); // Reference to the "events" table

        storage = FirebaseStorage.getInstance("gs://itmdaproject-3cb4b.appspot.com");
        imageRef = storage.getReference("images");

        editTextEventName = findViewById(R.id.editTextEventName);
        editTextTicketNum = findViewById(R.id.editTextTicketNum);
        editTextTicketPrice = findViewById(R.id.editTextTicketPrice);
        editTextYear = findViewById(R.id.editTextYear);
        editTextMonth = findViewById(R.id.editTextMonth);
        editTextDay = findViewById(R.id.editTextDay);
        editTextDescription = findViewById(R.id.editTextDescription);
        imageRef = storage.getReference("images");
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> register());

        Button uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(v -> uploadImage());

    }

    private void register() {
        Log.d("register", "register method called");
        String eventName = editTextEventName.getText().toString();
        String ticketNum = editTextTicketNum.getText().toString();
        String ticketPrice = editTextTicketPrice.getText().toString();
        String event_date = editTextYear.getText().toString();
        String month = editTextMonth.getText().toString();
        String day = editTextDay.getText().toString();
        String desc = editTextDescription.getText().toString().toLowerCase();
        String userID = getCurrentUserID();

        // Perform validation checks on the entered values
        if (eventName.isEmpty() || ticketNum.isEmpty() || ticketPrice.isEmpty() || event_date.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int monthInt;
        int dayInt;
        int yearInt;

        try {
            monthInt = Integer.parseInt(month);
            dayInt = Integer.parseInt(day);
            yearInt = Integer.parseInt(event_date);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (monthInt < 1 || monthInt > 12) {
            Toast.makeText(this, "Month must be between 1 and 12", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dayInt < 1 || dayInt > 31) {
            Toast.makeText(this, "Day must be between 1 and 31", Toast.LENGTH_SHORT).show();
            return;
        }

        if (yearInt < 2000 || yearInt > 3000) {
            Toast.makeText(this, "Year must be between 2000 and 3000", Toast.LENGTH_SHORT).show();
            return;
        }

        event_date = String.format("%04d-%02d-%02d", yearInt, monthInt, dayInt);

        Event event = new Event(eventName, userID, ticketNum, ticketPrice, event_date, month, day, "", desc);

        // Store the event in the database under the event name as the key
        eventRef.child(eventName).setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event registered successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(add_event.this, event_page.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to register event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });}

    private String getCurrentUserID() {
        // Get the currently logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is logged in
        if (currentUser != null) {
            // If the user is logged in, return their unique ID
            return currentUser.getUid();
        } else {
            return null;
        }
    }

    private void uploadImage() {
        // Open the gallery to select an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the image URI from the selected image
            Uri imageUri = data.getData();

            // Get a reference to the location where the image will be stored in Firebase Storage
            String imageName = System.currentTimeMillis() + ".jpg";
            StorageReference imageStorageRef = imageRef.child(imageName); // Use the class member imageRef

            // Upload the image to Firebase Storage
            imageStorageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, now get the download URL
                        imageStorageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    // Save the download URL to the "image" field in the event object
                                    String imageUrl = uri.toString();
                                    Event event = createEventObjectWithImage(imageUrl); // Create the Event object with the image URL

                                    // Store the event in the database under the event name as the key
                                    eventRef.child(event.getEvent_name()).setValue(event)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Event registered successfully!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(add_event.this, event_page.class);
                                                startActivity(intent);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Failed to register event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    // Failed to get the download URL
                                    Toast.makeText(this, "Failed to get image download URL.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Failed to upload the image
                        Toast.makeText(this, "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private Event createEventObjectWithImage(String imageUrl) {
        // Retrieve the entered values for the event
        String eventName = editTextEventName.getText().toString();
        String ticketNum = editTextTicketNum.getText().toString();
        String ticketPrice = editTextTicketPrice.getText().toString();
        String event_date = editTextYear.getText().toString();
        String month = editTextMonth.getText().toString();
        String day = editTextDay.getText().toString();
        String desc = editTextDescription.getText().toString().toLowerCase();
        String userID = getCurrentUserID();

        // Create the Event object with the image URL
        return new Event(eventName, userID, ticketNum, ticketPrice, event_date, month, day, imageUrl, desc);
    }

    public static class Event {
        private String event_name;
        private String host;
        private String totalTickets;
        private String ticketPrice;
        private String event_date;
        private String month;
        private String day;
        private String image;
        private String desc;

        public Event() {
            // Default constructor required for Firebase
        }

        public Event(String event_name, String host, String totalTickets, String ticketPrice, String event_date, String month, String day, String image, String desc) {
            this.event_name = event_name;
            this.host = host;
            this.totalTickets = totalTickets;
            this.ticketPrice = ticketPrice;
            this.event_date = event_date;
            this.month = month;
            this.day = day;
            this.image = image;
            this.desc = desc;
        }

        public String getEvent_name() {
            return event_name;
        }

        public String getHost() {
            return host;
        }

        public String getTotalTickets() {
            return totalTickets;
        }

        public String getTicketPrice() {
            return ticketPrice;
        }

        public String getEvent_date() {
            return event_date;
        }

        public String getMonth() {
            return month;
        }

        public String getDay() {
            return day;
        }

        public String getImage() {
            return image;
        }

        public String getDesc() {
            return desc;
        }
    }
}