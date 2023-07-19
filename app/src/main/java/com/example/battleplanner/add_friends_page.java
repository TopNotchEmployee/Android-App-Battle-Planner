package com.example.battleplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class add_friends_page extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference accountsRef;
    private DatabaseReference friendsListRef;
    private EditText editTextText;
    private LinearLayout fieldLayout;
    private String loggedInUserId;
    private String friendUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_page);

        mDatabase = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/");
        friendsListRef = mDatabase.getReference("friendsList");
        accountsRef = mDatabase.getReference("accounts");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loggedInUserId = currentUser.getUid();
            String loggedInUserEmail = currentUser.getEmail();
            String loggedInUsername = currentUser.getDisplayName();
        }

        editTextText = findViewById(R.id.editTextText);
        Button addFriendButton = findViewById(R.id.addFriend);
        fieldLayout = findViewById(R.id.fieldLayout);

        editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used in this case
            }

            @Override
            public void afterTextChanged(Editable s) {
                String enteredUsername = s.toString().trim();
                filterUsernames(enteredUsername);
            }
        });

        addFriendButton.setOnClickListener(v -> {
            String friendUsername = editTextText.getText().toString().trim();
            addFriendToFriendList(friendUsername);
        });

        fetchUsernames();
    }

    private void fetchUsernames() {
        accountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> usernamesList = new ArrayList<>();
                for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                    String username = accountSnapshot.child("username").getValue(String.class);
                    usernamesList.add(username);
                }
                updateUsernameDisplay(usernamesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(add_friends_page.this, "Error fetching usernames", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUsernameDisplay(List<String> usernames) {
        fieldLayout.removeAllViews(); // Clear existing views

        for (String username : usernames) {
            // Inflate friend_items XML layout
            View friendItemView = getLayoutInflater().inflate(R.layout.friends_items, null);

            // Get references to the views in the friend_items layout
            TextView usernameTextView = friendItemView.findViewById(R.id.textViewUsername);
            TextView statusTextView = friendItemView.findViewById(R.id.textViewStatusOnlineOrOffline);

            // Set the values for the views based on the friend data
            usernameTextView.setText(username);
            statusTextView.setText("Online"); // You can set the appropriate online/offline status here

            // Set an OnClickListener for the friend item view
            friendItemView.setOnClickListener(view -> {
                String friendUsername = usernameTextView.getText().toString();
                addFriendToFriendList(friendUsername);
            });

            // Add the friend_items view to the LinearLayout container
            fieldLayout.addView(friendItemView);
        }
    }

    private void filterUsernames(String enteredUsername) {
        accountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> filteredUsernames = new ArrayList<>();

                for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                    String username = accountSnapshot.child("username").getValue(String.class);
                    if (username != null && username.toLowerCase().contains(enteredUsername.toLowerCase())) {
                        filteredUsernames.add(username);
                    }
                }

                updateUsernameDisplay(filteredUsernames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(add_friends_page.this, "Error filtering usernames", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFriendToFriendList(String friendUsername) {
        DatabaseReference accountsRef = mDatabase.getReference("accounts");
        Query friendUsernameQuery = accountsRef.orderByChild("username").equalTo(friendUsername);
        friendUsernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String friendId = "";
                    for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                        friendId = accountSnapshot.getKey();
                        friendUserEmail = accountSnapshot.child("email").getValue(String.class);
                    }
                    assert friendId != null;
                    if (friendId.equals(loggedInUserId)) {
                        Toast.makeText(add_friends_page.this, "You cannot add yourself as a friend", Toast.LENGTH_SHORT).show();
                    } else {
                        DatabaseReference loggedInUserFriendsRef = friendsListRef.child(loggedInUserId);
                        DatabaseReference friendUserFriendsRef = friendsListRef.child(friendId);

                        // Update the friends list for the logged-in user
                        Map<String, Object> loggedInUserFriendUpdates = new HashMap<>();
                        loggedInUserFriendUpdates.put(friendId, true);
                        loggedInUserFriendsRef.child("friends").updateChildren(loggedInUserFriendUpdates);

                        // Update the friends list for the friend user
                        Map<String, Object> friendUserFriendUpdates = new HashMap<>();
                        friendUserFriendUpdates.put(loggedInUserId, true);
                        friendUserFriendsRef.child("friends").updateChildren(friendUserFriendUpdates);

                        // Retrieve the current username for the logged-in user
                        DatabaseReference currentUserRef = accountsRef.child(loggedInUserId);
                        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String loggedInUsername = dataSnapshot.child("username").getValue(String.class);

                                    // Update the username for the logged-in user
                                    loggedInUserFriendsRef.child("username").setValue(loggedInUsername);

                                    // Update the username for the friend user
                                    Map<String, Object> friendUserUpdates = new HashMap<>();
                                    friendUserUpdates.put("username", friendUsername);
                                    friendUserFriendsRef.updateChildren(friendUserUpdates);

                                    Toast.makeText(add_friends_page.this, "Friend added successfully", Toast.LENGTH_SHORT).show();
                                    editTextText.setText("");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(add_friends_page.this, "Error adding friend", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(add_friends_page.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(add_friends_page.this, "Error querying usernames", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
