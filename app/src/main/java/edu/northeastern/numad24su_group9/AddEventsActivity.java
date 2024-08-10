package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
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

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import edu.northeastern.numad24su_group9.checks.LocationChecker;
import edu.northeastern.numad24su_group9.firebase.DatabaseConnector;
import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.firebase.repository.database.TripRepository;
import edu.northeastern.numad24su_group9.firebase.repository.database.UserRepository;
import edu.northeastern.numad24su_group9.gemini.GeminiClient;
import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.model.GeneratedEvent;
import edu.northeastern.numad24su_group9.model.Trip;
import edu.northeastern.numad24su_group9.model.User;
import edu.northeastern.numad24su_group9.recycler.EventAdapter;
import edu.northeastern.numad24su_group9.recycler.GeneratedEventsAdapter;

public class AddEventsActivity extends AppCompatActivity {
    private List<Event> eventData;
    private List<Event> selectedEvents;
    private List<GeneratedEvent> selectedGeneratedEvents;
    private EventAdapter eventAdapter;
    private List<GeneratedEvent> generatedEvents;
    private List<String> selectedEventIDs = new ArrayList<>();
    private GeneratedEventsAdapter generatedEventsAdapter;
    private ProgressBar progressBar;
    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);

        trip = (Trip) getIntent().getSerializableExtra("trip");

        progressBar = findViewById(R.id.progressBar);
        selectedEvents = new ArrayList<>();
        selectedGeneratedEvents = new ArrayList<>();
        eventAdapter = new EventAdapter();
        generatedEventsAdapter = new GeneratedEventsAdapter();

        if (savedInstanceState != null) {
            eventData = (List<Event>) savedInstanceState.getSerializable("eventData");
            progressBar.setVisibility(View.GONE);
            showEvents();
        } else {
            getEvents();
            showEvents();
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
            if (selectedGeneratedEvents.isEmpty()) {
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
            } else {
                for (GeneratedEvent generatedEvent : selectedGeneratedEvents) {
                    selectedEventIDs.add(generatedEvent.getId());
                    DatabaseReference eventRef = DatabaseConnector.getInstance().getGeneratedEventsReference().child(generatedEvent.getId());
                    eventRef.setValue(generatedEvent);
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

        // Create a ThreadPoolExecutor
        int numThreads = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

        executor.submit(() -> {
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
                            if (event.isWithinDateRange(event.getStartDate(), trip.getStartDate(), trip.getEndDate()) && LocationChecker.isSameLocation(event.getLocation(), trip.getLocation())) {
                                eventData.add(event);
                            }
                        } else {
                            eventData.add(event);
                        }
                    }
                }
            }).addOnFailureListener(Throwable::printStackTrace);
        });
    }

    public void showEvents() {
        if (!(eventData.isEmpty())) {
            progressBar.setVisibility(View.GONE);
            updateUI(eventData);
        } else {
            Log.e("Event", "No events found");
            // Handle the case where no events were found
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Change Trip Details");
            builder.setMessage("Would you like to change your trip details?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Navigate to the trip details screen
                finish();
            });
            builder.setNegativeButton("No", (dialog, which) -> {

                SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                String uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

                // Get a reference to the user's data in the database
                User user = new User();
                UserRepository userRepository = new UserRepository(uid);

                Task<DataSnapshot> task1 = userRepository.getUserRef().get();
                task1.addOnSuccessListener(dataSnapshot1 -> {
                    if (dataSnapshot1.exists()) {
                        List<String> interests = new ArrayList<>();
                        for (DataSnapshot tripSnapshot : dataSnapshot1.child("interests").getChildren()) {
                            String interest = tripSnapshot.getValue(String.class);
                            interests.add(interest);
                        }
                        user.setInterests(interests);
                    }
                    // Ask Gemini to provide event recommendations
                    // Create a ThreadPoolExecutor
                    int numThreads1 = Runtime.getRuntime().availableProcessors();
                    ThreadPoolExecutor executor1 = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads1);

                    GeminiClient geminiClient = new GeminiClient();

                    Toast.makeText(this, "Generating suggested events", Toast.LENGTH_SHORT).show();
                    String query = "can you suggest me places in " + trip.getLocation() + "? My budget is " + trip.getMaxBudget() + ", in which I wish to include meals and transport. My date and time of availability is " + trip.getStartDate() + " " + trip.getStartTime() +" to " + trip.getEndDate() + " " + trip.getEndTime() + ". My interests are " + user.getInterests() + " I want the name of the place, suggested time and date to visit, along with brief description. For meals I want suggested place name, suggested cuisine, and expected costs. For transport I want the suggestion of the transport mode that will fall within budget, and the time taken. I want this is a consistent readable format with no Day 1 or Day 2 details. Meantion details using titles 'place', 'time', 'description', 'date";
                    ListenableFuture<GenerateContentResponse> response = geminiClient.generateResult(query);

                    // Generate trip name using Gemini API
                    Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onSuccess(GenerateContentResponse result) {
                            runOnUiThread(() -> {
                                extractInfo(result.getText());
                            });
                        }

                        @Override
                        public void onFailure(@NonNull Throwable t) {
                            // Handle the failure on the main thread
                            Log.e("RecommendationAlgorithm", "Error: " + t.getMessage());
                        }
                    }, executor1);
                }).addOnFailureListener(e -> Log.e("UserRepository", "Error retrieving user data: " + e.getMessage()));
            });
            builder.show();
        }
    }

    public void extractInfo(String text) {
        generatedEvents = new ArrayList<>();
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("**Place:**")) {
                GeneratedEvent generatedEvent = new GeneratedEvent();
                generatedEvent.setId("generatedEvent_" + UUID.randomUUID().toString());
                String place = line.substring(10).trim();
                generatedEvent.setTitle(place);
                String description = lines[i].substring(14).trim();
                generatedEvent.setDescription(description);
                generatedEvents.add(generatedEvent);
            }
        }

        if (generatedEvents.isEmpty()) {
            Toast.makeText(AddEventsActivity.this, "No events could be created", Toast.LENGTH_SHORT).show();
            finish();
        }

        updateUIGenerated(generatedEvents);

    }

    private void updateUIGenerated(List<GeneratedEvent> generatedEvents) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        generatedEventsAdapter = new GeneratedEventsAdapter();
        generatedEventsAdapter.updateData(generatedEvents);
        generatedEventsAdapter.setOnItemSelectListener((event) -> {
            if (selectedGeneratedEvents.contains(event)) {
                selectedGeneratedEvents.remove(event);
            } else {
                selectedGeneratedEvents.add(event);
            }
        });
        recyclerView.setAdapter(generatedEventsAdapter);
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
            showEvents();
        }
    }
}
