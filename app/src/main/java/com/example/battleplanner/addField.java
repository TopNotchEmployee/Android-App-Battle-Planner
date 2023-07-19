package com.example.battleplanner;

import android.os.Bundle;
import android.text.style.URLSpan;
import android.widget.Button;
import android.widget.EditText;
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

import java.net.URI;
import java.util.Objects;

public class addField extends AppCompatActivity {

    private DatabaseReference fieldRef;
    private DatabaseReference businessRef;
    private EditText editTextFieldName;
    private EditText editTextRules;
    private EditText editTextLocation;
    private EditText editTextRange;
    private EditText editTextRental;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_field);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://itmdaproject-3cb4b-default-rtdb.firebaseio.com/");
        fieldRef = database.getReference("fields");
        businessRef = database.getReference("business_accounts");
        editTextFieldName = findViewById(R.id.editTextFieldName);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextRange = findViewById(R.id.editTextRange);
        editTextRental = findViewById(R.id.editTextRental);
        editTextRules = findViewById(R.id.editTextRules);



        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> register());
    }

    private void register(){
        String fieldName = editTextFieldName.getText().toString().toLowerCase();
        String location = editTextLocation.getText().toString().toLowerCase();
        String range = editTextRange.getText().toString().toLowerCase();
        String rental = editTextRules.getText().toString().toLowerCase();
        String rules = editTextRules.getText().toString().toLowerCase();


        // Perform validation checks on the entered values
        if (fieldName.isEmpty() || location.isEmpty() || range.isEmpty() || rental.isEmpty() || rules.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        // Check if the field and bussiness already exist in the database
        Query business_nameQuery = businessRef.orderByChild("businessID").equalTo(fieldName);
        business_nameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.exists())) {
                    Toast.makeText(addField.this, "Business does not exist", Toast.LENGTH_SHORT).show();
                } else {
                    Query emailQuery = fieldRef.orderByChild("field_name").equalTo(fieldName);
                    emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(addField.this, "Field name already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                // Create a new field object
                                addField.Field field = new addField.Field(fieldName, location, range, rental, rules);

                                // Save the user object to the Realtime Database under the generated key
                                fieldRef.push().setValue(field);

                                Toast.makeText(addField.this, "Registration successful", Toast.LENGTH_SHORT).show();                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(addField.this, "Failed to check field name uniqueness", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addField.this, "Failed to check business existence", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public static class Field {
        private String fieldName;
        private String location;
        private String range;
        private String rental;
        private String rules;

        public Field() {
            // Default constructor required for Firebase
        }

        public Field(String fieldName, String location, String range, String rental, String rules) {
            this.fieldName = fieldName;
            this.location = location;
            this.range = range;
            this.rental = rental;
            this.rules = rules;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getLocation() {
            return location;
        }

        public String getRange() {
            return range;
        }

        public String getRental() {
            return rental;
        }

        public String getRules() {
            return rules;
        }
    }




}