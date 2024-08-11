package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;

import edu.northeastern.numad24su_group9.firebase.repository.database.UserRepository;
import edu.northeastern.numad24su_group9.gemini.GeminiClient;
import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.model.User;
import edu.northeastern.numad24su_group9.recycler.EventAdapter;
import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RightNowActivity extends AppCompatActivity {

    private EventAdapter eventAdapter;
    private RecyclerView recyclerView;
    private List<Event> allEvents;
    private ProgressBar progressBar;
    private FloatingActionButton registerEventButton;
    private String uid;

    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_right_now);

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        // Find the views
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setItemViewCacheSize(20); // Cache 20 views in memory
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        registerEventButton = findViewById(R.id.register_event_button); // Initialize the FloatingActionButton
        SearchView searchView = findViewById(R.id.RightNowSearchView);

        if (progressBar == null) {
            Log.e("RightNowActivity", "progressBar is null");
        }
        registerEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterEventActivity.class);
            startActivity(intent);
            finish();
        });
        if (searchView == null) {
            Log.e("RightNowActivity", "searchView is null");
        }

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
                        startActivity(new Intent(RightNowActivity.this, RightNowActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_budget) {
                        startActivity(new Intent(RightNowActivity.this, PlanningTripActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_profile) {
                        startActivity(new Intent(RightNowActivity.this, ProfileActivity.class));
                        return true;
                    }
                    return false;
                }
            });
        }

        // Set click listeners for the buttons
        if (registerEventButton != null) {
            registerEventButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegisterEventActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (searchView != null) {
            searchView.setOnClickListener(v -> searchView.setIconified(false));
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
        }
        if (savedInstanceState != null) {
            allEvents = (List<Event>) savedInstanceState.getSerializable("allEvents");
            backPressedTime = savedInstanceState.getLong("backPressedTime");
            updateUI(allEvents);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            getEvents();
        }
    }

    public void getEvents() {

        new Thread(() -> {
            allEvents = new ArrayList<>();
            EventRepository eventRepository = new EventRepository();

            Task<DataSnapshot> task = eventRepository.getEventRef().get();
            task.addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        Event event = new Event();
                        // Populate the event object
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
                        event.setIsReported(eventSnapshot.child("isReported").getValue(Boolean.class));
                        allEvents.add(event);
                    }

                    // Update the UI on the main thread
                    runOnUiThread(this::getUserRegistrationPattern);
                }
            }).addOnFailureListener(e -> {
                // Handle the failure case on the main thread
                runOnUiThread(e::printStackTrace);
            });
        }).start();
    }

    // Get user registration pattern from firebase
    private void getUserRegistrationPattern() {
        Runnable getUserRegistrationPatternTask = () -> {
            User user = new User();
            user.setUserID(uid);

            UserRepository userRepository = new UserRepository(uid);
            Task<DataSnapshot> task = userRepository.getUserRef().get();
            task.addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    List<String> userInterests = new ArrayList<>();
                    for (DataSnapshot interestSnapshot : dataSnapshot.child("interests").getChildren()) {
                        String interest = interestSnapshot.getValue(String.class);
                        userInterests.add(interest);
                    }

                    List<String> eventsAttendedIDs = new ArrayList<>();
                    for (DataSnapshot tripSnapshot : dataSnapshot.child("eventsAttended").getChildren()) {
                        String eventID = tripSnapshot.getValue(String.class);
                        eventsAttendedIDs.add(eventID);
                    }
                    generateEventRecommendations(eventsAttendedIDs, userInterests);
                }
            }).addOnFailureListener(e -> {
                // Handle any exceptions that occur during the database query
                Log.e("UserRepository", "Error retrieving user data: " + e.getMessage());
            });
        };

        // Post the getUserRegistrationPatternTask to the same background thread as getEvents()
        new Thread(getUserRegistrationPatternTask).start();
    }

    // Generate event recommendations based on user registration pattern
    private void generateEventRecommendations(List<String> eventsAttendedIDs, List<String> userInterests) {
        List<String> eventsAttended = new ArrayList<>();
        List<Event> recommendedEvents = new ArrayList<>();

        for (String eventID : eventsAttendedIDs) {
            for (Event event : allEvents) {
                if (event.getEventID().equals(eventID)) {
                    eventsAttended.add(event.getTitle());
                }
            }
        }

        // Ask Gemini to provide event recommendations
        // Create a ThreadPoolExecutor
        int numThreads = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

        GeminiClient geminiClient = new GeminiClient();
        ListenableFuture<GenerateContentResponse> response = geminiClient.generateResult("Can you recommend the events that the user would like based on the user's already registered events? The user likes the following events " + eventsAttended + "and these are the events we have:" + allEvents + "and the user interests are " + userInterests);

        // Generate trip name using Gemini API
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(GenerateContentResponse result) {
                List<String> recommendedEventTitles = extractTitles(result.getText());
                for (String title : recommendedEventTitles) {
                    for (Event event : allEvents) {
                        if (event.getTitle().equals(title)) {
                            recommendedEvents.add(event);
                        }
                    }
                }

                for (Event event : allEvents) {
                    if (!(recommendedEvents.contains(event))) {
                        recommendedEvents.add(event);
                    }
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    updateUI(recommendedEvents);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                // Handle the failure on the main thread
                Log.e("RecommendationAlgorithm", "Error: " + t.getMessage());
            }
        }, executor);
    }

    // Extract event titles from the Gemini response
    public static List<String> extractTitles(String input) {
        List<String> titles = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\*\\*(.+?)\\:\\*\\*");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            titles.add(matcher.group(1));
        }
        return titles;
    }

    private void updateUI(List<Event> events) {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this);
        eventAdapter.updateData(events);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(RightNowActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            intent.putExtra("previousActivity", "RightNowActivity");
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }

    private void filterEvents(String query) {
        HandlerThread handlerThread = new HandlerThread("FilterEventsThread");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());

        handler.post(() -> {
            List<Event> filteredEvents = new ArrayList<>();
            for (Event event : allEvents) {
                if (event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        event.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredEvents.add(event);
                }
            }

            // Update the UI on the main thread
            runOnUiThread(() -> {
                eventAdapter.updateData(filteredEvents);
                recyclerView.setAdapter(eventAdapter);
            });
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) backToast.cancel();
            moveTaskToBack(true);
        } else {
            backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save any necessary data
        outState.putSerializable("allEvents", new ArrayList<>(allEvents));
        outState.putLong("backPressedTime", backPressedTime);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the saved data
        if (savedInstanceState != null) {
            allEvents = (List<Event>) savedInstanceState.getSerializable("allEvents");
            backPressedTime = savedInstanceState.getLong("backPressedTime");
            updateUI(allEvents); // Make sure to update the UI with restored data
        }
    }
}