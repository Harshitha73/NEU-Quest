package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.firebase.repository.database.TripRepository;
import edu.northeastern.numad24su_group9.gemini.GeminiClient;
import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.model.Trip;
import edu.northeastern.numad24su_group9.recycler.EventAdapter;

public class AddEventsActivity extends AppCompatActivity {
    private List<Event> eventData;
    private List<Event> selectedEvents;
    private EventAdapter eventAdapter;
    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);

        trip = (Trip) getIntent().getSerializableExtra("trip");

        selectedEvents = new ArrayList<>();
        eventAdapter = new EventAdapter();

        // Find the buttons
        SearchView searchView = findViewById(R.id.searchView);

        // Set up the search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });

        getEvents();

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            Log.e("RightNowActivity", "bottomNavigationView is null");
        } else {
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                }
            });
        }
    }

    public void confirmSelection(View view) {
        if (selectedEvents.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
        } else {

            // Add the selected events to the trip
            List<String> selectedEventIDs = new ArrayList<>();
            for (Event event : selectedEvents) {
                selectedEventIDs.add(event.getEventID());
            }
            trip.setEventIDs(selectedEventIDs);

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

    private void filterEvents(String query) {
        // Implement your logic to filter the event items based on the search query
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : eventData) {
            if (event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    event.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredEvents.add(event);
            }
        }
        eventAdapter.updateData(filteredEvents);
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
                        if (event.isWithinDateRange(event.getStartDate(), trip.getStartDate(), trip.getEndDate()) && event.getLocation().equals(trip.getLocation())) {
                            eventData.add(event);
                        }
                    } else {
                        eventData.add(event);
                    }
                }
                if (!(eventData.isEmpty())) {
                    Log.e("Event", "Events found");
                    Log.e("EventData", eventData.toString());
                    updateUI(eventData);
                } else {
                    Log.e("Event", "No events found");
                    Toast.makeText(this, "No events found", Toast.LENGTH_SHORT).show();
                    // Handle the case where no events were found
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Change Trip Details");
                    builder.setMessage("Would you like to change your trip details?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        // Navigate to the trip details screen
                        Intent intent = new Intent(AddEventsActivity.this, PlanningTripActivity.class);
                        startActivity(intent);
                        finish();
                    });
                    builder.setNegativeButton("No", (dialog, which) -> {
                        // Do nothing
                        // Ask Gemini to provide event recommendations
                        // Create a ThreadPoolExecutor
                        int numThreads = Runtime.getRuntime().availableProcessors();
                        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

                        GeminiClient geminiClient = new GeminiClient();

                        ListenableFuture<GenerateContentResponse> response = geminiClient.generateResult("Can you recommend a trip for following trip details: " + trip);

                        // Generate trip name using Gemini API
                        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onSuccess(GenerateContentResponse result) {
                                Log.e("RecommendationAlgorithm", "Success");
                                Log.e("RecommendationAlgorithm", Objects.requireNonNull(result.getText()));
                            }

                            @Override
                            public void onFailure(@NonNull Throwable t) {
                                // Handle the failure on the main thread
                                Log.e("RecommendationAlgorithm", "Error: " + t.getMessage());
                            }
                        }, executor);
                        finish();
                    });
                    builder.show();
                }
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateUI(List<Event> events) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
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
}
