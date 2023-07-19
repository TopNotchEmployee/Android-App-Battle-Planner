package com.example.battleplanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class friends_chats_page extends AppCompatActivity {
    private Toolbar toolbar;
    private int selectedColor = Color.GRAY;
    private String friendId;

    private UserOnlineStatusManager onlineStatusManager;
    private boolean isBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_chats_page);
        Intent intent = getIntent();

        onlineStatusManager = new UserOnlineStatusManager();

        // Retrieve the friendId from the intent
        friendId = intent.getStringExtra("friendId");

        // Initialize the toolbar
        toolbar = findViewById(R.id.toolbar);

        // Retrieve the friend's username from the intent extras or any other source
        String friendUsername = intent.getStringExtra("friendUsername");

        // Make sure the friendUsername is not null before proceeding
        if (friendUsername != null) {
            // Set the friend's username in the view
            TextView friendsChatUsername = findViewById(R.id.friendsChatUsername);
            friendsChatUsername.setText(friendUsername);
        }

        // Set up the settings button and its click listener
        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> showSettingsPopup());

        // Retrieve the current user's ID from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference friendsListRef = FirebaseDatabase.getInstance().getReference("friendsList")
                    .child(currentUserId).child("color").child(friendId);

            friendsListRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        int color = dataSnapshot.getValue(Integer.class);
                        toolbar.setBackgroundColor(color);
                    } else {
                        toolbar.setBackgroundColor(Color.GRAY);
                    }
                } else {
                    toolbar.setBackgroundColor(Color.GRAY);
                }
            });
        }
    }

    private void updateFriendCardColor(int color) {
        toolbar.setBackgroundColor(color);
        selectedColor = color;

        // Save the selected color in the Firebase Realtime Database
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference friendsListRef = FirebaseDatabase.getInstance().getReference("friendsList")
                    .child(currentUserId).child("friends").child(friendId).child("color");

            friendsListRef.setValue(color).addOnCompleteListener(colorTask -> {
                if (colorTask.isSuccessful()) {
                    Log.d("updateFriendCardColor", "Color saved successfully");
                } else {
                    Log.e("updateFriendCardColor", "Failed to save color");
                    Toast.makeText(getApplicationContext(), "Failed to save color", Toast.LENGTH_SHORT).show();
                }
            });

            // Send an intent to the friends_page with the color value
            Intent sendIntent = new Intent(friends_chats_page.this, friends_page.class);
            sendIntent.putExtra("color", color);
            startActivity(sendIntent);
        }
    }


    private void showSettingsPopup() {
        Log.d("friends_chats_page", "showSettingsPopup() method called");

        // Retrieve the current user's authentication information from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is authenticated
        if (currentUser != null) {
            // Retrieve the unique user ID
            String currentUserId = currentUser.getUid();

            // Retrieve the friend's status from the Firebase Realtime Database
            DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("friendsList")
                    .child(currentUserId).child("friends").child(friendId);

            friendsRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    boolean isBlocked = false;

                    if (dataSnapshot.exists()) {
                        isBlocked = dataSnapshot.getValue(Boolean.class);
                    }

                    // Create the instance of the SettingsPopupDialog and pass the necessary parameters
                    // Create the instance of the SettingsPopupDialog and pass the necessary parameters
                    SettingsPopupDialog settingsPopupDialog = new SettingsPopupDialog(this, isBlocked, friendId, selectedColor -> {

                        // Store the selected color in a variable
                        int colorValue = selectedColor;

                        // Implement the desired functionality when a color is selected
                        toolbar.setBackgroundColor(colorValue);
                        this.selectedColor = colorValue;

                        // Save the selected color in the Firebase Realtime Database
                        DatabaseReference colorRef = FirebaseDatabase.getInstance().getReference("friendsList")
                                .child(currentUserId).child("color").child(friendId);
                        colorRef.setValue(colorValue).addOnCompleteListener(colorTask -> {
                            if (colorTask.isSuccessful()) {
                                Log.d("showSettingsPopup", "Color saved successfully");
                            } else {
                                Log.e("showSettingsPopup", "Failed to save color");
                                Toast.makeText(getApplicationContext(), "Failed to save color", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }, selectedColor);

                    // Show the settings popup dialog
                    settingsPopupDialog.show(getSupportFragmentManager(), "settingsPopupDialog");
                } else {
                    Log.e("showSettingsPopup", "Failed to retrieve friend's status");
                    Toast.makeText(getApplicationContext(), "Failed to retrieve friend's status", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("showSettingsPopup", "User is not authenticated");
            Toast.makeText(getApplicationContext(), "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }


    // Custom DialogFragment class for the settings popup dialog
    public static class SettingsPopupDialog extends DialogFragment {
        private OnColorSelectedListener onColorSelectedListener;
        private String friendId;
        private int selectedColor;
        private boolean isBlocked;
        private Context context;

        public SettingsPopupDialog(Context context, boolean isBlocked, String friendId, OnColorSelectedListener listener, int selectedColor) {
            this.context = context;
            this.isBlocked = isBlocked;
            this.friendId = friendId;
            this.onColorSelectedListener = listener;
            this.selectedColor = selectedColor;
            this.optionsList = new ArrayList<>(Arrays.asList("Option 1", "Option 2", "Block Friend"));
            this.arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, optionsList);
        }



        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Settings")
                    .setItems(new CharSequence[]{"Remove Friend", "Block Friend", "Change Color"},
                            (dialog, which) -> {
                                // Handle the selected option
                                switch (which) {
                                    case 0:
                                        // Remove Friend selected
                                        removeFriend(requireContext(), friendId);
                                        break;
                                    case 1:
                                        // Block Friend selected
                                        toggleBlockFriend();
                                        break;
                                    case 2:
                                        // Change Color selected
                                        showColorPickerDialog();
                                        break;
                                }

                            });
            return builder.create();
        }

        private void removeFriend(Context context, String friendId) {
            // Retrieve the current user's authentication information from Firebase Authentication
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            // Check if the user is authenticated
            if (user != null) {
                // Retrieve the unique user ID
                String userId = user.getUid();

                // Retrieve the current user's friends list from the Firebase Realtime Database
                DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("friendsList").child(userId).child("friends");

                // Remove the friend from the friends list using their unique ID
                friendsRef.child(friendId).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Friend removed successfully
                        Toast.makeText(context, "Friend removed successfully", Toast.LENGTH_SHORT).show();
                        // Perform any additional actions or display a success message

                        // Navigate back to the friends_page
                        dismiss();
                    } else {
                        // Failed to remove friend
                        Toast.makeText(context, "Failed to remove friend", Toast.LENGTH_SHORT).show();
                        // Display an error message or take appropriate action
                    }
                });

            }
        }
        private ArrayAdapter<String> arrayAdapter;
        private List<String> optionsList;

        private void toggleBlockFriend() {
            isBlocked = !isBlocked;
            String blockOptionText = isBlocked ? "Unblock Friend" : "Block Friend";

            if (optionsList != null) {
                // Find the index of the "Block Friend" option in the list
                int blockOptionIndex = optionsList.indexOf("Block Friend");

                if (blockOptionIndex != -1) {
                    // Update the option text at the specific index
                    optionsList.set(blockOptionIndex, blockOptionText);

                    // Notify the adapter about the data set change
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            if (isBlocked) {
                blockFriend();
            } else {
                unblockFriend();
            }
        }

// Initialize the optionsList and arrayAdapter in your code

        private void blockFriend() {
            // Retrieve the current user's authentication information from Firebase Authentication
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            // Check if the user is authenticated
            if (user != null) {
                // Retrieve the unique user ID
                String userId = user.getUid();

                // Retrieve the current user's friends list from the Firebase Realtime Database
                DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("friendsList")
                        .child(userId).child("friends");

                // Set the friend's status to false (blocked)
                friendsRef.child(friendId).setValue(false)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Friend blocked successfully
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Friend blocked successfully", Toast.LENGTH_SHORT).show();
                                }
                                // Perform any additional actions or display a success message

                                // Navigate back to the friends_page
                                dismiss();
                            } else {
                                // Failed to block friend
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Failed to block friend", Toast.LENGTH_SHORT).show();
                                }
                                // Display an error message or take appropriate action
                            }
                        });
            }
        }

        private void unblockFriend() {
            // Retrieve the current user's authentication information from Firebase Authentication
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            // Check if the user is authenticated
            if (user != null) {
                // Retrieve the unique user ID
                String userId = user.getUid();

                // Retrieve the current user's friends list from the Firebase Realtime Database
                DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("friendsList")
                        .child(userId).child("friends");

                // Set the friend's status to true (unblocked)
                friendsRef.child(friendId).setValue(true)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Friend unblocked successfully
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Friend unblocked successfully", Toast.LENGTH_SHORT).show();
                                }
                                // Perform any additional actions or display a success message

                                // Navigate back to the friends_page
                                dismiss();
                            } else {
                                // Failed to unblock friend
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Failed to unblock friend", Toast.LENGTH_SHORT).show();
                                }
                                // Display an error message or take appropriate action
                            }
                        });
            }
        }

        private void showColorPickerDialog() {
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
            colorPickerDialog.setInitialColor(selectedColor);
            colorPickerDialog.setOnColorSelectedListener(color -> {
                if (onColorSelectedListener != null) {
                    onColorSelectedListener.onColorSelected(color);
                    selectedColor = color;
                }
            });
            colorPickerDialog.show(getParentFragmentManager(), "colorPickerDialog");
        }
    }

    public interface OnColorSelectedListener {
        void onColorSelected(Integer color);
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
        Intent resultIntent = new Intent();
        resultIntent.putExtra("friendColorValue", selectedColor);
        resultIntent.putExtra("friendId", friendId);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

}
