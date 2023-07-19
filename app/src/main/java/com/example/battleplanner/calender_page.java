package com.example.battleplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class calender_page extends AppCompatActivity implements calendar_adapter.OnItemListener {

    public interface EventInfoListener {
        void onEventInfoReceived(String eventInfo);
    }


    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private List<String> importantDates; // List to store important event dates

    private DatabaseReference databaseReference; // Reference to the Firebase Realtime Database

    List<String> eventNames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_page);
        initWidgets();
        selectedDate = LocalDate.now();

        // Initialize the Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/").getReference().child("events");

        fetchImportantDatesAndEventNames();
    }

    private void fetchImportantDatesAndEventNames() {

        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //getting the user ID



        DatabaseReference bookingsRef = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/")
                .getReference().child("bookings");

        //Only looks at events booked for by the currently logged in user also known as "currentUserID"
        bookingsRef.orderByChild("userID").equalTo(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //This is all my previous code unchanged
                List<String> dates = new ArrayList<>();
                List<String> eventNames = new ArrayList<>();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String eventID = childSnapshot.child("eventID").getValue(String.class);

                    // Retrieve the event details based on the eventID from the bookings
                    DatabaseReference eventsRef = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/")
                            .getReference().child("events").child(eventID);
                    eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String date = dataSnapshot.child("event_date").getValue(String.class);
                            String eventName = dataSnapshot.child("event_name").getValue(String.class);

                            dates.add(date);
                            eventNames.add(eventName);

                            // Update the importantDates and eventNames lists
                            importantDates = dates;
                            calender_page.this.eventNames = eventNames;

                            // Set the month view after fetching the data
                            setMonthView();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Error handling
                            Log.d("Error", "onCancelled: " + databaseError.toString());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error Handling
                Log.d("Error", "onCancelled: " + databaseError.toString());
            }
        });
    }



    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
    }





    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<calendar_day> daysInMonth = daysInMonthArray(selectedDate);

        calendar_adapter calendarAdapter = new calendar_adapter(daysInMonth, importantDates, this, databaseReference);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }


    private ArrayList<calendar_day> daysInMonthArray(LocalDate date) {
        ArrayList<calendar_day> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add(new calendar_day("", new ArrayList<>(), false));
            } else {
                String day = String.valueOf(i - dayOfWeek);

                // Check if the importantDates list is not null and contains the current date
                boolean isImportant = false;
                List<String> events = new ArrayList<>();

                if (importantDates != null) {
                    LocalDate currentDate = selectedDate.withDayOfMonth(Integer.parseInt(day));

                    for (int j = 0; j < importantDates.size(); j++) {
                        if (importantDates.get(j).equals(currentDate.toString())) {
                            isImportant = true;
                            events.add(eventNames.get(j));
                        }
                    }
                }

                daysInMonthArray.add(new calendar_day(day, events, isImportant));
            }
        }

        return daysInMonthArray;
    }



    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }


    public void onItemClick(int position, String dayText) {
        String date = dayText + " " + monthYearText.getText().toString();
        String events = "";

        for (int i = 0; i < importantDates.size(); i++) {
            if (importantDates.get(i).equals(date)) {
                events += eventNames.get(i) + "\n";
            }
        }

        if (events.isEmpty()) {
            events = "No events";
        }

        // Send the events information to the EventInfoListener
        // This can be used to display the events in a dialog or any other desired way
        EventInfoListener listener = (EventInfoListener) this;
        listener.onEventInfoReceived(events);
    }
}
