package edu.northeastern.numad24su_group9;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.firebase.repository.database.TripRepository;
import edu.northeastern.numad24su_group9.firebase.repository.database.UserRepository;
import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.model.Trip;
import edu.northeastern.numad24su_group9.recycler.EventAdapter;

public class AddEventsActivity extends AppCompatActivity {
    private List<Event> eventData;
    private List<Event> selectedEvents;
    private EventAdapter eventAdapter;
    private List<String> selectedEventIDs = new ArrayList<>();
    private Trip trip;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);

        trip = (Trip) getIntent().getSerializableExtra("trip");

        selectedEvents = new ArrayList<>();
        eventAdapter = new EventAdapter();
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        if (progressBar == null) {
            Log.e("AddEventsActivity", "progressBar is null");
        }

        if (savedInstanceState != null) {
            eventData = (List<Event>) savedInstanceState.getSerializable("eventData");
            if (!(eventData.isEmpty())) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                updateUI(eventData);
            } else {
                progressBar.setVisibility(View.GONE);
                // Handle the case where no events were found
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Change Trip Details");
                builder.setMessage("No events found within the specified date range and location. Would you like to change the trip details?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    // Navigate to the trip details screen
                    finish();
                });
                builder.show();
            }
        } else {
            getEvents();
        }

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            Log.e("RightNowActivity", "bottomNavigationView is null");
        } else {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(AddEventsActivity.this, RightNowActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    startActivity(new Intent(AddEventsActivity.this, PlanningTripActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(AddEventsActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    public void confirmSelection(View view) {
        if (selectedEvents.isEmpty()) {
            Toast.makeText(this, "Please select at least one event", Toast.LENGTH_SHORT).show();
        } else {
            for (Event event : selectedEvents) {
                selectedEventIDs.add(event.getEventID());
            }
            trip.setEventIDs(selectedEventIDs);

            SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            String uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

            // Get a reference to the user's data in the database
            UserRepository userRepository = new UserRepository(uid);
            DatabaseReference userRef = userRepository.getUserRef();
            DatabaseReference userItineraryRef = userRef.child("plannedTrips").push();
            userItineraryRef.setValue(trip.getTripID());

            // Save trip in the database
            TripRepository tripRepository = new TripRepository();
            DatabaseReference tripRef = tripRepository.getTripRef().child(trip.getTripID());
            tripRef.setValue(trip);

            Toast.makeText(this, "Trip saved successfully", Toast.LENGTH_SHORT).show();
            finish();

            Intent intent = new Intent(AddEventsActivity.this, RightNowActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void getEvents() {
        eventData = new ArrayList<>();

        EventRepository eventRepository = new EventRepository();

        Task<DataSnapshot> task = eventRepository.getEventRef().get();
        // Handle any exceptions that occur during the database query
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = new Event();
                    event.setEventID(eventSnapshot.getKey());
                    event.setTitle(eventSnapshot.child("title").getValue(String.class));
                    event.setImage(eventSnapshot.child("image").getValue(String.class));
                    event.setDescription(eventSnapshot.child("description").getValue(String.class));
                    event.setStartTime(eventSnapshot.child("startTime").getValue(String.class));
                    event.setStartDate(eventSnapshot.child("startDate").getValue(String.class));
                    event.setEndTime(eventSnapshot.child("endTime").getValue(String.class));
                    event.setEndDate(eventSnapshot.child("endDate").getValue(String.class));
                    event.setPrice(eventSnapshot.child("price").getValue(String.class));
                    event.setLocation(eventSnapshot.child("location").getValue(String.class));
                    event.setRegisterLink(eventSnapshot.child("registerLink").getValue(String.class));

                    if (!Objects.equals(event.getStartDate(), "")) {
                        if (event.isWithinDateRange(event.getStartDate(), trip.getStartDate(), trip.getEndDate())) {
                            eventData.add(event);
                        }
                    } else {
                        eventData.add(event);
                    }
                }
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                updateUI(eventData);
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateUI(List<Event> events) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter();
        eventAdapter.updateData(events);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(AddEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        eventAdapter.setOnItemSelectListener((event) -> {
            if (selectedEvents.contains(event)) {
                selectedEvents.remove(event);
            } else {
                selectedEvents.add(event);
            }
        });
        recyclerView.setAdapter(eventAdapter);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save any necessary data
        outState.putSerializable("eventData", new ArrayList<>(eventData));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the saved data
        if (savedInstanceState != null) {
            eventData = (List<Event>) savedInstanceState.getSerializable("eventData");
            if (!(eventData.isEmpty())) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                updateUI(eventData);
            } else {
                progressBar.setVisibility(View.GONE);
                // Handle the case where no events were found
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Change Trip Details");
                builder.setMessage("No events found within the specified date range and location. Would you like to change the trip details?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    // Navigate to the trip details screen
                    finish();
                });
                builder.show();
            }
        }
    }
}