package edu.northeastern.numad24su_group9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.recycler.AdminConsoleAdapter;

public class AdminConsole extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminConsoleAdapter adapter;
    private ArrayList<Event> allEvents;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_console);

        progressBar = findViewById(R.id.AdminConsoleProgressBar);
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
                        startActivity(new Intent(AdminConsole.this, RightNowActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_budget) {
                        startActivity(new Intent(AdminConsole.this, PlanningTripActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_profile) {
                        startActivity(new Intent(AdminConsole.this, ProfileActivity.class));
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void getEvents() {
        allEvents = new ArrayList<>();
        EventRepository eventRepository = new EventRepository();

        Task<DataSnapshot> task = eventRepository.getEventRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    if (Boolean.TRUE.equals(eventSnapshot.child("isReported").getValue(Boolean.class))) {
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
                        event.setIsReported(eventSnapshot.child("isReported").getValue(Boolean.class));
                        allEvents.add(event);
                    }
                }
            }
            updateUI(allEvents);
        }
        ).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateUI(List<Event> events) {
        recyclerView = findViewById(R.id.adminConsoleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(20); // Cache 20 views in memory
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        adapter = new AdminConsoleAdapter(this);
        adapter.updateData(events);
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}