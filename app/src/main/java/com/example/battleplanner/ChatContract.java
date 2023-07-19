package com.example.battleplanner;

import android.provider.BaseColumns;

public final class ChatContract {
    private ChatContract() {
        // Private constructor to prevent instantiation
    }

    /**
     * Inner class representing the structure of the "messages" table in the database.
     */
    public static class MessageEntry implements BaseColumns {
        // Table name
        public static final String TABLE_NAME = "messages";

        // Column names
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_SENDER = "sender";
        public static final String COLUMN_RECEIVER = "receiver";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
