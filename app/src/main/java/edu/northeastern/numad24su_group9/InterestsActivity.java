package edu.northeastern.numad24su_group9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterestsActivity extends AppCompatActivity {

    private LinearLayout interestsContainer;
    private List<String> selectedInterests;
    private String uid,name;
    private List<String> interests;
    private List<CheckBox> interestCheckboxes;

    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        interestsContainer = findViewById(R.id.interests_container);
        Button saveButton = findViewById(R.id.save_button);
        selectedInterests = new ArrayList<>();

        // Add some sample interests
        interests = new ArrayList<>(Arrays.asList("Art", "Nature", "Photography", "Travel", "Music", "Movies", "Food", "Sports"));

        uid = getIntent().getStringExtra("uid");
        name = getIntent().getStringExtra("name");

        // Get an instance of the Firebase Realtime Database
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        interestCheckboxes = new ArrayList<>();
        populateInterestOptions();

        saveButton.setOnClickListener(v -> saveInterests());
    }

    private void populateInterestOptions() {
        firebaseDatabase.child("Users").child(uid).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> existingInterests = new ArrayList<>();
                for (DataSnapshot interestSnapshot : snapshot.getChildren()) {
                    existingInterests.add(interestSnapshot.getValue(String.class));
                }
                for (String interest : interests) {
                    CheckBox checkbox = new CheckBox(InterestsActivity.this);
                    checkbox.setText(interest);
                    checkbox.setTextColor(getResources().getColor(android.R.color.black));
                    checkbox.getButtonDrawable().setTint(getResources().getColor(android.R.color.black));
                    if (existingInterests.contains(interest)) {
                        checkbox.setChecked(true);
                    }
                    interestsContainer.addView(checkbox);
                    interestCheckboxes.add(checkbox);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InterestsActivity.this, "Error loading interests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveInterests() {
        selectedInterests.clear();
        for (CheckBox checkbox : interestCheckboxes) {
            if (checkbox.isChecked()) {
                selectedInterests.add(checkbox.getText().toString());
            }
        }
        Log.d("InterestsActivity", "Selected interests: " + selectedInterests);
        if (selectedInterests.isEmpty()) {
            Toast.makeText(this, "Please select at least one interest", Toast.LENGTH_SHORT).show();
        } else {
            // Do something with the selected interests, e.g., save them to the database
          //  addUserToDatabase();
            updateUserInterests();
        }
    }
    private void updateUserInterests() {
        // Create a map with the data you want to set
        Map<String, Object> userData = new HashMap<>();
        userData.put("interests", selectedInterests);

        // Get a reference to the user's data in the database
        DatabaseReference userRef = firebaseDatabase.child("Users").child(uid);

        // Update user interests
        userRef.child("interests").setValue(selectedInterests)
                .addOnSuccessListener(aVoid -> {
                    // Data has been successfully updated in the database
                    Toast.makeText(InterestsActivity.this, "Interests updated", Toast.LENGTH_SHORT).show();

                    // Go back to ProfileActivity
                    Intent intent = new Intent(InterestsActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occurred during the update operation
                    Toast.makeText(InterestsActivity.this, "Error updating interests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addUserToDatabase() {

        // Create a map with the data you want to set
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("interests", selectedInterests);

        // Get a reference to the user's data in the database
        DatabaseReference userRef = firebaseDatabase.child("Users").child(uid);

        // Check if the user's UID already exists in the database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // The user's UID does not exist, so create a new entry
                    userRef.setValue(userData)
                            .addOnSuccessListener(aVoid -> {
                                // Data has been successfully written to the database
                                Toast.makeText(InterestsActivity.this, "User data saved", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(InterestsActivity.this, RightNowActivity.class);
                                intent.putExtra("name", name);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Handle any errors that occurred during the write operation
                                Toast.makeText(InterestsActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the data retrieval
                Toast.makeText(InterestsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}