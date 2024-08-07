package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;

import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.recycler.EventAdapter;
import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;

import java.util.ArrayList;
import java.util.List;

public class RightNowActivity extends AppCompatActivity {

    private EventAdapter eventAdapter;
    private RecyclerView recyclerView;
    private List<Event> allEvents;
    private ProgressBar progressBar;
    private FloatingActionButton registerEventButton;

    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_right_now);

        // Find the views
        progressBar = findViewById(R.id.progressBar);
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

        getEvents();
    }

    public void getEvents() {
        allEvents = new ArrayList<>();
        EventRepository eventRepository = new EventRepository();

        Task<DataSnapshot> task = eventRepository.getEventRef().get();
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
                    allEvents.add(event);
                }
                updateUI(allEvents);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateUI(List<Event> events) {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter();
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
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    event.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredEvents.add(event);
            }
        }
        eventAdapter.updateData(filteredEvents);
        recyclerView.setAdapter(eventAdapter);
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
}