package com.example.battleplanner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserOnlineStatusManager {
    private DatabaseReference onlineStatusRef;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public UserOnlineStatusManager() {
        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        // Set onlineStatusRef to the appropriate database reference
        if (currentUser != null) {
            onlineStatusRef = database.getReference("users/" + currentUser.getUid() + "/online");
        }
    }

    public void setOnlineStatus(boolean isOnline) {
        if (currentUser != null) {
            if (isOnline) {
                onlineStatusRef.setValue(true);
                onlineStatusRef.onDisconnect().setValue(false);
            } else {
                onlineStatusRef.setValue(false);
            }
        }
    }
}
