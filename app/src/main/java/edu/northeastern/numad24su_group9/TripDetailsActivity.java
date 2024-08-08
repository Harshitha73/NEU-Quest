package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.model.Trip;
import edu.northeastern.numad24su_group9.recycler.TimelineEventAdapter;

public class TripDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Event> allEvents;
    private TimelineEventAdapter eventAdapter;
    private Trip trip;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        recyclerView = findViewById(R.id.recycler_view);

        Intent intent = getIntent();
        if (intent == null) {
            Log.e("TripDetailsActivity", "Intent is null");
            finish();
            return;
        }

        trip = (Trip) intent.getSerializableExtra("trip");
        Log.d("TripDetailsActivity", "Trip object from Intent: " + trip);

        // Check if the trip is null
        if (trip == null) {
            Log.e("TripDetailsActivity", "Trip object is null");
            finish();
            return;
        }

        TextView tripNameTextView = findViewById(R.id.trip_name);
        TextView tripBudgetTextView = findViewById(R.id.trip_budget);
        TextView tripPreferencesTextView = findViewById(R.id.trip_preferences);
        TextView tripTimeTextView = findViewById(R.id.trip_time);

        tripNameTextView.setText(trip.getTitle());
        tripTimeTextView.setText(getCurrentTimeString(Long.parseLong(trip.getTripID())));
        tripBudgetTextView.setText("Budget from $" + trip.getMinBudget() + " - $" + trip.getMaxBudget());

        boolean mealsIncluded = Boolean.parseBoolean(trip.getMealsIncluded());
        boolean transportIncluded = Boolean.parseBoolean(trip.getTransportIncluded());

        if (mealsIncluded && !transportIncluded) {
            tripPreferencesTextView.setText("Meal included in budget");
        } else if (!mealsIncluded && transportIncluded) {
            tripPreferencesTextView.setText("Transportation included in budget");
        } else if (mealsIncluded && transportIncluded) {
            tripPreferencesTextView.setText("Transportation and meals included in budget");
        } else if (!mealsIncluded && !transportIncluded) {
            tripPreferencesTextView.setText("Budget only for the trip. No meals or transportation included");
        }

        getEvents();
    }

    private static String getCurrentTimeString(long millis) {
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();
        Date currentDate = new Date(millis);
        return dateTimeFormat.format(currentDate);
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
                    if(trip.getEventIDs().contains(event.getEventID())) {
                        allEvents.add(event);
                    }
                }
                Log.e("RecommendationAlgorithm", "Events retrieved");
                Log.e("RecommendationAlgorithm", "All events: " + allEvents);
                updateUI(allEvents);
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateUI(List<Event> events) {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new TimelineEventAdapter();
        eventAdapter.updateData(events);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(TripDetailsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TripDetailsActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}