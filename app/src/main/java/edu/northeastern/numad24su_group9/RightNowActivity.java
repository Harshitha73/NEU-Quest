package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
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

    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_right_now);

        // Find the buttons
        FloatingActionButton registerEventButton = findViewById(R.id.register_button);
        FloatingActionButton userProfile = findViewById(R.id.user_profile_fab);
        SearchView searchView = findViewById(R.id.searchView);

        // Set click listeners for the buttons
        registerEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterEventActivity.class);
            startActivity(intent);
            finish();
        });

        userProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });

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
    }

    public void getEvents() {
        allEvents = new ArrayList<>();

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
                    allEvents.add(event);
                }
                updateUI(allEvents);
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateUI(List<Event> allEvents) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        EventAdapter eventAdapter = new EventAdapter();
        eventAdapter.updateData(allEvents);
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
        // Implement your logic to filter the event items based on the search query
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