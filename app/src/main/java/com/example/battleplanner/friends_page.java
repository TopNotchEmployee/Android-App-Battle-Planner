package com.example.battleplanner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class friends_page extends AppCompatActivity {
    private DatabaseReference usersRef;
    private LinearLayout fieldLayout;
    private TextView displayFriends;
    private UserOnlineStatusManager onlineStatusManager;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_page);
        // Retrieve the intent
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    // Update the color based on the result from friends_chats_page activity
                    int friendColorValue = data.getIntExtra("friendColorValue", 0);
                    String friendId = data.getStringExtra("friendId");

                    // Find the corresponding cardView and update its background color
                    for (int i = 0; i < fieldLayout.getChildCount(); i++) {
                        View friendItemView = fieldLayout.getChildAt(i);
                        String itemViewFriendId = friendItemView.getTag().toString();
                        if (itemViewFriendId.equals(friendId)) {
                            CardView cardView = friendItemView.findViewById(R.id.cardView);
                            String friendColor = String.format("#%06X", (0xFFFFFF & friendColorValue));
                            cardView.setCardBackgroundColor(Color.parseColor(friendColor));
                            break;
                        }
                    }
                }
            }
        });

        // Initialize buttons
        Button calendarButton = findViewById(R.id.button3);
        Button eventButton = findViewById(R.id.button2);
        Button homeButton = findViewById(R.id.button);
        View friendItemView = getLayoutInflater().inflate(R.layout.friends_items, null);
        onlineStatusManager = new UserOnlineStatusManager();
        // Retrieve the intent
        // Set click listeners for buttons to start corresponding activities
        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(friends_page.this, calender_page.class);
            startActivity(intent);
        });
        eventButton.setOnClickListener(v -> {
            Intent intent = new Intent(friends_page.this, event_page.class);
            startActivity(intent);
        });
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(friends_page.this, home_page.class);
            startActivity(intent);
        });

        // Initialize layout and text view
        fieldLayout = findViewById(R.id.fieldLayout);
        displayFriends = findViewById(R.id.displayFriends);

        // Retrieve the user ID from the intent
        String loggedInUser = getIntent().getStringExtra("userId");

        // Initialize Firebase Database and reference
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/");
        usersRef = database.getReference("friendsList");

        // Retrieve the logged-in user's friend list from the Firebase Realtime Database
        DatabaseReference friendsListRef = usersRef.child(loggedInUser).child("friends");
        friendsListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fieldLayout.removeAllViews(); // Clear existing views

                if (dataSnapshot.exists()) {
                    for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                        final String friendId = friendSnapshot.getKey();
                        assert friendId != null;
                        DatabaseReference friendUsernameRef = usersRef.child(friendId).child("username");
                        friendUsernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    String friendUsername = dataSnapshot.getValue(String.class);

                                    // Retrieve the friend's profile picture URL
                                    DatabaseReference friendProfilePicRef = FirebaseDatabase.getInstance().getReference().child("accounts").child(friendId).child("profilePicUrl");
                                    friendProfilePicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String friendProfilePicUrl = dataSnapshot.getValue(String.class);

                                            // Debugging statements
                                            Log.d("FriendProfilePic", "friendId: " + friendId);
                                            Log.d("FriendProfilePic", "usersRef: " + usersRef.toString());
                                            Log.d("FriendProfilePic", "friendProfilePicUrl: " + friendProfilePicUrl);

                                            // Inflate friend_items XML layout
                                            View friendItemView = getLayoutInflater().inflate(R.layout.friends_items, null);
                                            ImageView profilePicImageView = friendItemView.findViewById(R.id.imageViewProfilePic);
                                            CardView cardView = friendItemView.findViewById(R.id.cardView);
                                            // Check if friendProfilePicUrl is null or empty
                                            if (friendProfilePicUrl != null && !friendProfilePicUrl.isEmpty()) {
                                                // Load and display the friend's profile picture using Picasso
                                                Picasso.get().load(friendProfilePicUrl).into(profilePicImageView);
                                            } else {
                                                // Display the login_bg image
                                                profilePicImageView.setImageResource(R.drawable.login_bg);
                                            }

                                            // Get references to the views in the friend_items layout
                                            TextView usernameTextView = friendItemView.findViewById(R.id.textViewUsername);
                                            TextView statusTextView = friendItemView.findViewById(R.id.textViewStatusOnlineOrOffline);

                                            // Set the values for the views based on the friend data
                                            usernameTextView.setText(friendUsername);

                                            // Retrieve the friend's online status from the Firebase Realtime Database
                                            DatabaseReference friendOnlineStatusRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendId).child("online");
                                            friendOnlineStatusRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        boolean isOnline = dataSnapshot.getValue(Boolean.class);

                                                        // Set the appropriate online/offline status
                                                        String status = isOnline ? "Online" : "Offline";
                                                        statusTextView.setText(status);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    // Handle any errors that occur during the retrieval
                                                    displayFriends.setText("Error retrieving friend list");
                                                }
                                            });

                                            // Read the color value from the database
                                            DatabaseReference friendColorRef = FirebaseDatabase.getInstance().getReference()
                                                    .child("friendsList")
                                                    .child(loggedInUser)
                                                    .child("color")
                                                    .child(friendId);

                                            friendColorRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        int friendColorValue = dataSnapshot.getValue(Integer.class);
                                                        // Convert the color value to a hexadecimal string
                                                        String friendColor = String.format("#%06X", (0xFFFFFF & friendColorValue));
                                                        // Change the card color if friendColor is not null
                                                        if (friendColor != null) {
                                                            cardView.setCardBackgroundColor(Color.parseColor(friendColor));
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    // Handle any errors that occur during the retrieval
                                                    displayFriends.setText("Error retrieving friend list");
                                                }
                                            });

                                            // Set an OnClickListener for the friend item view
                                            friendItemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    // Start friends_chats_page activity with the selected friend's information
                                                    Intent intent = new Intent(friends_page.this, friends_chats_page.class);
                                                    intent.putExtra("friendId", friendId);
                                                    intent.putExtra("friendUsername", friendUsername);
                                                    startActivity(intent);
                                                }
                                            });

                                            // Add the friend item view to the parent layout
                                            fieldLayout.addView(friendItemView);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle any errors that occur during the retrieval
                                            displayFriends.setText("Error retrieving friend list");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle any errors that occur during the retrieval
                                displayFriends.setText("Error retrieving friend list");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the retrieval
                displayFriends.setText("Error retrieving friend list");
            }
        });

        Button addFriendButton = findViewById(R.id.btnAddFriends);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(friends_page.this, add_friends_page.class);
                startActivity(intent);
            }
        });
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
