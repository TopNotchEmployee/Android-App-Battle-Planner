package com.example.battleplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;



import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;




import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class event_page extends AppCompatActivity {

    private List<Event> events;
    private DatabaseReference databaseReference;
    private String userId; // User ID of the current user
    private UserOnlineStatusManager onlineStatusManager;

    private LinearLayout eventContainer;
    private LinearLayout eventLayout;



    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Retrieve the selectedFieldId from the intent
        String selectedFieldId = getIntent().getStringExtra("selectedFieldId");

        Log.d("FieldID", "onCreate: The Field ID is " + selectedFieldId);


        setContentView(R.layout.activity_event_page);

        //Buttons for navigation

        Button calendarButton = findViewById(R.id.buttonCalender);
        Button homeButton = findViewById(R.id.buttonHome);
        Button btnGoFriends = findViewById(R.id.buttonFriends);

        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(event_page.this, calender_page.class);
            startActivity(intent);
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(event_page.this, home_page.class);
            startActivity(intent);
        });


        btnGoFriends.setOnClickListener(v -> {
            Intent intent = new Intent(event_page.this, friends_page.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        onlineStatusManager = new UserOnlineStatusManager();

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            userId = currentUser.getUid();
            String userName = currentUser.getDisplayName();
            Log.d("Username", "Username " + userName);
        }




        ScrollView scrollView = findViewById(R.id.scrollViewEventPage);
        eventContainer = findViewById(R.id.eventLayout);

        eventLayout = new LinearLayout(this);
        eventLayout.setOrientation(LinearLayout.VERTICAL);

        events = new ArrayList();
        loadEvents();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            View eventView = getLayoutInflater().inflate(R.layout.event_item, null);

            TextView nameTextView = eventView.findViewById(R.id.textViewEventName);

            nameTextView.setText(event.getName());

            eventLayout.addView(eventView);
        }

        eventContainer.addView(eventLayout);
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
    private void loadEvents() {
        home_page homePage = (home_page) getIntent().getSerializableExtra("homePage");

        String selectedFieldId = getIntent().getStringExtra("selectedFieldId");

        //This code here is for loading the events page with intent. So if you want specific events to show up use their field ID and they will load on the top of the page.
        if (selectedFieldId != null && !selectedFieldId.isEmpty())
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("events").orderByChild("fieldID").equalTo(selectedFieldId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        String name = eventSnapshot.child("event_name").getValue(String.class);
                        Log.d("onDataChange", "Event Name: " + name);
                        String dateTime = eventSnapshot.child("event_date").getValue(String.class);
                        String imageUrl = eventSnapshot.child("event_image_url").getValue(String.class);
                        String fieldID = eventSnapshot.child("fieldID").getValue(String.class);
                        String price = eventSnapshot.child("ticketPrice").getValue(String.class);

                        Event event = new Event(name, dateTime, imageUrl, fieldID, price);
                        events.add(event);
                    }

                    // After loading events, update the UI
                    updateEventsUI(events);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });}
        //This code is for when the events page is loaded from elsewhere and just shows all the events that are on the database
        else
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("events").addListenerForSingleValueEvent(new ValueEventListener() {

                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        String name = eventSnapshot.child("event_name").getValue(String.class);
                        Log.d("onDataChange", "Event Name: " + name);
                        String dateTime = eventSnapshot.child("event_date").getValue(String.class);
                        String imageUrl = eventSnapshot.child("image").getValue(String.class);
                        String fieldID = eventSnapshot.child("fieldID").getValue(String.class);
                        String price = eventSnapshot.child("ticketPrice").getValue(String.class);

                        Event event = new Event(name, dateTime, imageUrl, fieldID, price);
                        events.add(event);
                    }

                    // After loading events, update the UI
                    updateEventsUI(events);
                }


                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }


            });

        }


    }


    //For all Events
    private void updateEventsUI(List<Event> events) {
        eventLayout.removeAllViews();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            View eventView = getLayoutInflater().inflate(R.layout.event_item, null);

            TextView nameTextView = eventView.findViewById(R.id.textViewEventName);
            Button bookButton = eventView.findViewById(R.id.buttonBook);
            ImageView eventImageView = eventView.findViewById(R.id.imageViewFieldsBusinesses);

            TextView dateTextView = eventView.findViewById(R.id.textViewEventDate);
            TextView priceTextView = eventView.findViewById(R.id.textViewEventPrice);

            nameTextView.setText(event.getName()); // Set the event name in the nameTextView
            dateTextView.setText("Date: " + event.getDateTime()); // Set the event name in the nameTextView
            priceTextView.setText("Price: R" + event.getTicketPrice()); // Set the event name in the nameTextView

            String imageUrl = event.getImageURL();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d("Image URL", "Loading image from URL: " + imageUrl);
                // Use Picasso to load the image into the eventImageView
                loadImageFromUrl(imageUrl, eventImageView);
            } else {
                Log.d("Image URL", "Image URL is empty or null.");
            }


            isEventBooked(event, new OnBookingCheckListener() {
                @Override
                public void onBookingChecked(boolean isBooked) {
                    if (isBooked) {
                        // Event is already booked by the user
                        bookButton.setText("Booked");
                        bookButton.setEnabled(true);  // Enable the button for unbooking
                        bookButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                unbookEvent(event);
                            }
                        });
                    } else {
                        // Event is not booked by the user
                        bookButton.setText("Book");
                        bookButton.setEnabled(true);
                        bookButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bookEvent(event);
                            }
                        });
                    }
                }
            });

            eventLayout.addView(eventView);
        }
    }


    //For Specific Events
    private void updateEventUI(Event event, boolean isBooked) {
        for (int i = 0; i < eventLayout.getChildCount(); i++) {
            View eventView = eventLayout.getChildAt(i);
            TextView nameTextView = eventView.findViewById(R.id.textViewEventName);
            Button bookButton = eventView.findViewById(R.id.buttonBook);

            String eventName = nameTextView.getText().toString();
            if (eventName.equals(event.getName())) {
                if (isBooked) {
                    bookButton.setText("Booked");
                    bookButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            unbookEvent(event);
                        }
                    });
                } else {
                    bookButton.setText("Book");
                    bookButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bookEvent(event);
                        }
                    });
                }
                break;
            }
        }
    }


    private void unbookEvent(Event event) {
        Log.d("User Unbooking", "unbookEvent: " + event.getName());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Find the booking for the event made by the user
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        Query query = bookingsRef.orderByChild("eventID").equalTo(event.getName());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot bookingSnapshot : dataSnapshot.getChildren()) {
                    String bookingUserId = bookingSnapshot.child("userID").getValue(String.class);
                    if (bookingUserId != null && bookingUserId.equals(userId)) {
                        // Delete the booking from the database
                        bookingSnapshot.getRef().removeValue();

                        // Update the events list
                        events.remove(event);

                        // Update the UI to reflect the unbooking
                        updateEventUI(event, false);

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }





    private void bookEvent(Event event) {
        Log.d("User Booking", "bookEvent: " + event.getName() + " Booked Date is " + getCurrentDate());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a new booking document in the "bookings" collection
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        DatabaseReference newBookingRef = bookingsRef.push();

        newBookingRef.child("eventID").setValue(event.getName());
        newBookingRef.child("userID").setValue(userId);
        newBookingRef.child("bookedDate").setValue(getCurrentDate());

        // Update the UI to reflect the booking
        updateEventUI(event, true);

        // Replace the URL below with the snapscan url when business is registered, the paymentGatewayUrl can be taken from the firebase realtime database if necessary
       String paymentGatewayUrl = "http://battleplannerpayment.infinityfreeapp.com";
       Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentGatewayUrl));
       startActivity(browserIntent);
    }

    private String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now); // Return the formatted date string
    }

    private void isEventBooked(Event event, OnBookingCheckListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check if the event is booked by the user
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        Query query = bookingsRef.orderByChild("eventID").equalTo(event.getName());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isBooked = false;

                for (DataSnapshot bookingSnapshot : dataSnapshot.getChildren()) {
                    String bookingUserId = bookingSnapshot.child("userID").getValue(String.class);
                    if (bookingUserId != null && bookingUserId.equals(userId)) {
                        isBooked = true;
                        break;
                    }
                }

                listener.onBookingChecked(isBooked);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    interface OnBookingCheckListener {
        void onBookingChecked(boolean isBooked);
    }





    private void loadImageFromUrl(String imageUrl, ImageView imageView) {
        Picasso.get()
                .load(imageUrl)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Image loaded successfully
                    }

                    @Override
                    public void onError(Exception e) {
                        // Error occurred while loading the image
                    }
                });
    }






}
