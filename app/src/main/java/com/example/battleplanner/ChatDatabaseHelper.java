package com.example.battleplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chat_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = ChatContract.MessageEntry.TABLE_NAME;
    private static final String COLUMN_ID = ChatContract.MessageEntry.COLUMN_ID;
    private static final String COLUMN_SENDER = ChatContract.MessageEntry.COLUMN_SENDER;
    private static final String COLUMN_RECEIVER = ChatContract.MessageEntry.COLUMN_RECEIVER;
    private static final String COLUMN_CONTENT = ChatContract.MessageEntry.COLUMN_CONTENT;
    private static final String COLUMN_TIMESTAMP = ChatContract.MessageEntry.COLUMN_TIMESTAMP;
    private static ChatDatabaseHelper dbHelper;

    // Method to initialize the ChatDatabaseHelper
    public static synchronized ChatDatabaseHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new ChatDatabaseHelper(context.getApplicationContext());
        }
        return dbHelper;
    }

    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the chat message table
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SENDER + " TEXT, " +
                COLUMN_RECEIVER + " TEXT, " +
                COLUMN_CONTENT  + " TEXT, " +
                COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the existing table and recreate it on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertMessage(String sender, String receiver, String message) {
        // Insert a new chat message into the database
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_RECEIVER, receiver);
        values.put(COLUMN_CONTENT , message);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<Message> getMessages (String sender, String receiver) {
        // Retrieve chat messages between the given sender and receiver
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {COLUMN_SENDER, COLUMN_RECEIVER, COLUMN_CONTENT , COLUMN_TIMESTAMP};
        String selection = "(" + COLUMN_SENDER + "=? AND " + COLUMN_RECEIVER + "=?) OR (" + COLUMN_SENDER + "=? AND " + COLUMN_RECEIVER + "=?)";
        String[] selectionArgs = {sender, receiver, receiver, sender};
        String orderBy = COLUMN_TIMESTAMP + " ASC";
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Retrieve message details from the cursor
                String messageSender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER));
                String messageReceiver = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECEIVER));
                String messageContent = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT ));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));

                // Create a new Message object and add it to the list
                Message message = new Message(messageSender, messageReceiver, messageContent, timestamp);
                messages.add(message);
            }
            cursor.close();
        }

        db.close();
        return messages;
    }
}
