package com.example.battleplanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class user_customization extends AppCompatActivity {

    private DatabaseReference usersRef;
    private static final int PICK_IMAGE = 1;
    private ImageView profileImageView;
    private UserOnlineStatusManager onlineStatusManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_customization);

        onlineStatusManager = new UserOnlineStatusManager();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/");
        usersRef = database.getReference("accounts");

        Button selectPicButton = findViewById(R.id.buttonSelectImage);
        profileImageView = findViewById(R.id.imageViewProfilePic);

        // Declare the ActivityResultLauncher
        ActivityResultLauncher<Intent> imagePickerLauncher;

        // Inside onCreate method, initialize the ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData();
                            try {
                                // Save the selected image in Firebase Storage under the user's ID
                                saveProfileImage(selectedImageUri);
                                // Set the selected image in the ImageView
                                profileImageView.setImageURI(selectedImageUri);
                                Toast.makeText(this, "Profile pic updated", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                // Handle any errors that occurred during the image conversion
                                // Display an error message to the user if needed
                                Toast.makeText(this, "Error saving profile pic", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        Button registerBusinessButton = findViewById(R.id.registerBusinessButton);
        registerBusinessButton.setOnClickListener(v -> {
            // Create an intent to navigate to the add_business activity
            Intent intent = new Intent(user_customization.this, add_business.class);
            startActivity(intent); // Start the add_business activity
        });

        // Update the button click listener to use the ActivityResultLauncher
        selectPicButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        String loggedInUsername = getLoggedInUsername();
        EditText editTextUsername = findViewById(R.id.editTextUsername);
        editTextUsername.setText(loggedInUsername);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String newUsername = editTextUsername.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                // Save the updated username to the database
                saveUpdatedUsername(newUsername);
            }

            // Create an intent to navigate to the home_page activity
            Intent intent = new Intent(user_customization.this, home_page.class);
            startActivity(intent); // Start the home_page activity
        });

        // Load the user's current profile picture if available
        loadUserProfilePicture();
    }

    private void loadUserProfilePicture() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = usersRef.child(userId);
            userRef.child("profilePicUrl").get().addOnSuccessListener(dataSnapshot -> {
                String imageUrl = dataSnapshot.getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // If a profile picture URL exists, load it into the ImageView
                    Picasso.get().load(imageUrl).into(profileImageView);
                }
            }).addOnFailureListener(e -> {
                // Handle any errors that occurred while retrieving the profile picture URL
                // Display an error message to the user if needed
            });
        }
    }

    private void saveProfileImage(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // Get the user's unique ID
            StorageReference profilePicRef = FirebaseStorage.getInstance().getReference("profile_pictures")
                    .child(userId + ".jpg"); // Use the user's ID as the filename
            UploadTask uploadTask = profilePicRef.putFile(imageUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image upload success. You can perform any additional operations here if needed.
                // For example, you can retrieve the download URL of the uploaded image and save it to the database.
                profilePicRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    String imageUrl = downloadUrl.toString();
                    DatabaseReference userRef = usersRef.child(userId); // Create a reference to the user's data using their ID
                    userRef.child("profilePicUrl").setValue(imageUrl); // Set the profile image URL under the user's ID in the database
                });
            }).addOnFailureListener(e -> Toast.makeText(this, "Error saving profile pic", Toast.LENGTH_SHORT).show());
        }
    }

    private String getLoggedInUsername() {
        // Assuming you are using Firebase Authentication and the user is already logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getDisplayName(); // Assuming the username is stored as the display name
        }
        return null; // Return null or an empty string if the username is not available
    }

    private void saveUpdatedUsername(String newUsername) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // Get the user's unique ID
            DatabaseReference userRef = usersRef.child(userId); // Create a reference to the user's data using their ID
            userRef.child("username").setValue(newUsername); // Set the username under the user's ID in the database
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Username Updated!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            try {
                // Save the selected image in Firebase Storage under the user's ID
                saveProfileImage(selectedImageUri);
                // Set the selected image in the ImageView
                profileImageView.setImageURI(selectedImageUri);
            } catch (Exception e) {
                // Handle any errors that occurred during the image conversion
                // Display an error message to the user if needed
                Toast.makeText(this, "Error saving profile pic", Toast.LENGTH_SHORT).show();
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
}
