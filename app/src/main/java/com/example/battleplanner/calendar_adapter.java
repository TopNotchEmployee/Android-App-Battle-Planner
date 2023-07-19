package com.example.battleplanner;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

class calendar_adapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private ArrayList<calendar_day> daysOfMonth;
    private final List<String> importantDates;
    private final OnItemListener onItemListener;

    private final DatabaseReference databaseReference; // Reference to the Firebase Realtime Database


    public calendar_adapter(ArrayList<calendar_day> daysOfMonth, List<String> importantDates, calender_page onItemListener, DatabaseReference databaseReference)
    {
        this.daysOfMonth = daysOfMonth;
        this.importantDates = importantDates;
        this.onItemListener = onItemListener;
        this.databaseReference = databaseReference;
    }



    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        calendar_day calendarDay = daysOfMonth.get(position);
        holder.dayOfMonth.setText(calendarDay.getDay());


        if (calendarDay.isImportant()) {
            holder.dayOfMonth.append("*"); // Append '*' to indicate important date
            holder.eventInfo.setVisibility(View.VISIBLE); // Show the event info TextView

            // Get the event information for the current day
            List<String> events = calendarDay.getEventInfo();

            // Set the event information TextView
            String eventInfo = "";

            for (String event : events)
            {
                //In case there are multiple Events booked for one day
                eventInfo += event + " ";

            }
            Log.d("Event Information: ", "The event/s on " + calendarDay.getDay() + " are: " + eventInfo);
            holder.eventInfo.setText(eventInfo);
        } else {
            holder.eventInfo.setVisibility(View.GONE); // Hide the event info TextView if it's not an event date
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public void setDaysOfMonth(ArrayList<calendar_day> daysInMonth) {
        daysOfMonth = daysInMonth;
    }

    public interface OnItemListener {

    }
}
