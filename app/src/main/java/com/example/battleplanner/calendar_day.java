package com.example.battleplanner;

import java.util.List;

class calendar_day {
    private String day;
    private List<String> eventInfo;
    private boolean important;

    public calendar_day(String day, List<String> eventInfo, boolean important) {
        this.day = day;
        this.eventInfo = eventInfo;
        this.important = important;
    }

    public String getDay() {
        return day;
    }

    public List<String> getEventInfo() {
        return eventInfo;
    }

    public boolean isImportant() {
        return important;
    }
}


