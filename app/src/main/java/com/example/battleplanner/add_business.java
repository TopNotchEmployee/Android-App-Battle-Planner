package com.example.battleplanner;
// Import necessary packages

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class add_business extends AppCompatActivity {
    private EditText editTextBusinessName;
    private EditText editTextBusinessEmail;
    private EditText editTextBusinessNumber;
    private EditText editTextBusinessLink;
    private EditText editTextBusinessLocation;
    private EditText editTextWhatsappGroup;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri;
    private DatabaseReference fieldsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_business);

        // Check if the user is logged in and authenticated
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // If the user is not logged in, redirect to the login activity
            startActivity(new Intent(this, login_page.class));
            finish(); // Close this activity to prevent the user from coming back here after logging in
            return;
        }

        // Initialize Firebase database reference under the "fields" table
        fieldsRef = database.getReference("fields");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mAuth = FirebaseAuth.getInstance();
        editTextBusinessName = findViewById(R.id.editTextBusinessName);
        editTextBusinessEmail = findViewById(R.id.editTextBusinessEmail);
        editTextBusinessNumber = findViewById(R.id.editTextBusinessNumber);
        editTextBusinessLink = findViewById(R.id.editTextBusinessLink);
        editTextBusinessLocation = findViewById(R.id.editTextBusinessLocation);
        editTextWhatsappGroup = findViewById(R.id.editTextWhatsappGroup);

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> register());

        Button uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(v -> openGallery());

    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Set the selected image to the ImageView for display
            ImageView profileImageView = findViewById(R.id.profileImageView);
            profileImageView.setImageURI(imageUri);
        }
    }

    private void register() {
        String field_name = editTextBusinessName.getText().toString();
        String email = editTextBusinessEmail.getText().toString();
        String number = editTextBusinessNumber.getText().toString();
        String link = editTextBusinessLink.getText().toString();
        String location = editTextBusinessLocation.getText().toString();
        String whatsappgroup = editTextWhatsappGroup.getText().toString();

        // Perform validation checks on the entered values
        if (field_name.isEmpty() || email.isEmpty() || number.isEmpty() || link.isEmpty() || location.isEmpty() || whatsappgroup.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the unique userID of the logged-in user
        String fieldOwner = mAuth.getCurrentUser().getUid();

        // If all fields are filled, create a Business object and save it to the database
        Business business = new Business(field_name, email, number, link, location, whatsappgroup, fieldOwner);

        // Generate a single key for the business data and image
        String businessKey = fieldsRef.push().getKey();
        if (businessKey == null) {
            Toast.makeText(this, "Failed to generate a unique key. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // If an image is selected, save it to Firebase Storage
        if (imageUri != null) {
            // Get a reference to the location where the image will be stored in Firebase Storage
            StorageReference imageRef = storageReference.child("images/" + businessKey + ".jpg");

            // Upload the image to Firebase Storage
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, now get the download URL
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    // Save the download URL to the "field_image_url" field in the business object
                                    String imageUrl = uri.toString();
                                    business.setField_image_url(imageUrl);

                                    // Save the business object to the database with the single generated key
                                    fieldsRef.child(businessKey).setValue(business)
                                            .addOnSuccessListener(aVoid -> {
                                                // Data successfully saved to the database
                                                Toast.makeText(this, "Business registered successfully", Toast.LENGTH_SHORT).show();
                                                // Optionally, you can navigate to another activity after successful registration
                                                startActivity(new Intent(add_business.this, home_page.class));
                                            })
                                            .addOnFailureListener(e -> {
                                                // Failed to save data to the database
                                                Toast.makeText(this, "Failed to register business. Please try again.", Toast.LENGTH_SHORT).show();
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
        } else {
            // If no image is selected, save the business object without the image URL
            // Save the business object to the database with the single generated key
            fieldsRef.child(businessKey).setValue(business)
                    .addOnSuccessListener(aVoid -> {
                        // Data successfully saved to the database
                        Toast.makeText(this, "Business registered successfully", Toast.LENGTH_SHORT).show();
                        // Optionally, you can navigate to another activity after successful registration
                        startActivity(new Intent(add_business.this, home_page.class));
                    })
                    .addOnFailureListener(e -> {
                        // Failed to save data to the database
                        Toast.makeText(this, "Failed to register business. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public class Business {
        private String field_name;
        private String email;
        private String number;
        private String link;
        private String location;
        private String whatsappgroup;
        private String field_image_url;
        private String fieldOwner;

        public Business() {
            // Default constructor required for Firebase
        }

        public Business(String field_name, String email, String number, String link, String location, String whatsappgroup, String fieldOwner) {
            this.field_name = field_name;
            this.email = email;
            this.number = number;
            this.link = link;
            this.location = location;
            this.whatsappgroup = whatsappgroup;
            this.fieldOwner = fieldOwner;
        }

        public String getField_name() {
            return field_name;
        }

        public String getEmail() {
            return email;
        }

        public String getNumber() {
            return number;
        }

        public String getLink() {
            return link;
        }

        public String getLocation() {
            return location;
        }

        public String getWhatsappGroup() {
            return whatsappgroup;
        }
        public String getField_image_url() {
            return field_image_url;
        }
        public String getFieldOwner() {
            return fieldOwner;
        }

        public void setField_image_url(String field_image_url) {
            this.field_image_url = field_image_url;
        }
    }
}
