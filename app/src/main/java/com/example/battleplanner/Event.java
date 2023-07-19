package com.example.battleplanner;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
    private final String name;        // Name of the event
    private final String dateTime;    // Date and time of the event
    private final String imageURL;    // URL of the event's image
    private final String fieldId;     // ID of the field associated with the event

    private final String ticketPrice;     // Ticket Price of Event

    // Parcelable methods
    protected Event(Parcel in) {
        name = in.readString();
        dateTime = in.readString();
        imageURL = in.readString();
        fieldId = in.readString();
        ticketPrice = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(dateTime);
        dest.writeString(imageURL);
        dest.writeString(fieldId);
        dest.writeString(ticketPrice);
    }

    // Constructor for creating an Event object
    public Event(String name, String dateTime, String imageURL, String fieldId, String ticketPrice)
    {
        this.name = name;
        this.dateTime = dateTime;
        this.imageURL = imageURL;
        this.fieldId = fieldId;
        this.ticketPrice = ticketPrice;
    }

    // Getter methods for retrieving event details
    public String getName() {
        return name;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getTicketPrice(){return ticketPrice;}

    public String getImageURL() {
        return imageURL;
    }
}
