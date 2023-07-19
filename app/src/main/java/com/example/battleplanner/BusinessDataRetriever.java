// BusinessDataRetriever.java

package com.example.battleplanner;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BusinessDataRetriever {

    public interface DataEntryListener {
        void onDataEntryLoaded(List<DataEntry> dataEntries);
        void onDataEntryError(DatabaseError databaseError);
    }

    //ITS ALL GOOD MAN
    // Inside BusinessDataRetriever class
    public static void fetchDataEntries(DataEntryListener listener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("fields");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DataEntry> dataEntries = new ArrayList<>();
                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    String fieldID = entrySnapshot.getKey();

                    // Retrieve the new field_image_url
                    String fieldImageUrl = entrySnapshot.child("field_image_url").getValue(String.class);

                    // ... Retrieve other fields as before
                    String fieldName = entrySnapshot.child("field_name").getValue(String.class);
                    String email = entrySnapshot.child("email").getValue(String.class);
                    String link = entrySnapshot.child("link").getValue(String.class);
                    String location = entrySnapshot.child("location").getValue(String.class);
                    String number = entrySnapshot.child("number").getValue(String.class);
                    String whatsappGroup = entrySnapshot.child("whatsappGroup").getValue(String.class);
                    String ownerID = entrySnapshot.child("fieldOwner").getValue(String.class);
                    int priority = entrySnapshot.child("priority").getValue(int.class);

                    DataEntry dataEntry = new DataEntry(email, fieldName, link, location, number
                            , whatsappGroup, fieldImageUrl, ownerID, priority);

                    dataEntries.add(dataEntry);

                    // Logging for verification
                    Log.d("DataEntry", "Field ID: " + fieldID +
                            ", Priority: " + priority +
                            ", Field Name: " + fieldName);


                    Log.d("Field info", "Field ID: " + fieldID + ", Email: " + email + ", Field Name: " + fieldName
                            + ", Link: " + link + ", Location: " + location
                            + ", Number: " + number + ", WhatsApp Group: " + whatsappGroup
                            + ", Image URL " + fieldImageUrl + ", Owner ID " + ownerID + ", Priority " + priority);


                }


                // Sort the dataEntries list based on the priority
                Collections.sort(dataEntries, new Comparator<DataEntry>() {
                    @Override
                    public int compare(DataEntry entry1, DataEntry entry2) {
                        return Integer.compare(entry2.getPriority(), entry1.getPriority());
                    }
                });

                // Logging for verification
                for (DataEntry entry : dataEntries) {
                    Log.d("SortedDataEntry", "Field Name: " + entry.getFieldName() +
                            ", Priority: " + entry.getPriority());
                }

                // Pass the entire list to the listener
                listener.onDataEntryLoaded(dataEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onDataEntryError(databaseError);
            }
        });
    }


    public static class DataRetrievalHandler implements BusinessDataRetriever.DataEntryListener {
        private BusinessDataRetriever businessDataRetriever = new BusinessDataRetriever();

        public void startDataRetrieval() {
            businessDataRetriever.fetchDataEntries(this);
        }

        @Override
        public void onDataEntryLoaded(List<BusinessDataRetriever.DataEntry> dataEntries) {
            // Logging for verification
            for (DataEntry entry : dataEntries) {
                Log.d("ReceivedDataEntry", "Field Name: " + entry.getFieldName() +
                        ", Priority: " + entry.getPriority());
            }
            // Process the retrieved dataEntries here
            for (BusinessDataRetriever.DataEntry dataEntry : dataEntries) {
                String fieldName = dataEntry.getFieldName();
                String email = dataEntry.getEmail();
                String link = dataEntry.getLink();
                String location = dataEntry.getLocation();
                String number = dataEntry.getNumber();
                String whatsappGroup = dataEntry.getWhatsappGroup();
                String ownerID = dataEntry.getOwnerId();
                int  priority = dataEntry.getPriority();
            }
        }

        @Override
        public void onDataEntryError(DatabaseError databaseError) {
            String errorMessage = databaseError.getMessage();
            Log.d("error", errorMessage);
        }
    }


    public static class DataEntry {
        public String email;
        public String fieldName;
        public String link;
        public String location;
        public String number;
        public String whatsappGroup;
        public String fieldImageUrl;
        private String ownerId;

        private int priority;  //This is what will hold the priority for the fields

        public DataEntry() {
            // Required empty constructor for Firebase
        }

        public DataEntry(String email, String fieldName, String link, String location,
                         String number, String whatsappGroup, String imageURL, String ownerId, int priority) {
            this.email = email;
            this.fieldName = fieldName;
            this.link = link;
            this.location = location;
            this.number = number;
            this.whatsappGroup = whatsappGroup;
            this.fieldImageUrl = imageURL;
            this.ownerId = ownerId;
            this.priority = priority;
        }

        public String getEmail() {
            return email;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getLink() {
            return link;
        }

        public String getLocation() {
            return location;
        }

        public String getNumber() {
            return number;
        }

        public String getWhatsappGroup() {
            return whatsappGroup;
        }
        public String getFieldImageUrl() {
            return fieldImageUrl;
        }
        public String getOwnerId() {
            return ownerId;
        }

        public int getPriority() {
            return priority;
        }
    }
}
