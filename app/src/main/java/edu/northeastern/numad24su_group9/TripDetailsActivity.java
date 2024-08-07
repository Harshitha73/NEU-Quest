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

    private List<Event> events;
    private Trip trip;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

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

        String title = trip.getTitle();

        getEvents();

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
    }

    private static String getCurrentTimeString(long millis) {
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();
        Date currentDate = new Date(millis);
        return dateTimeFormat.format(currentDate);
    }

    public void getEvents() {
        EventRepository eventRepository = new EventRepository();
        events = new ArrayList<>();

        Task<DataSnapshot> task = eventRepository.getEventRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for(String eventID : trip.getEventIDs()) {
                    Event event = new Event();
                    event.setTitle(dataSnapshot.child(eventID).child("title").getValue(String.class));
                    event.setImage(dataSnapshot.child(eventID).child("image").getValue(String.class));
                    event.setDescription(dataSnapshot.child(eventID).child("description").getValue(String.class));
                    event.setStartTime(dataSnapshot.child(eventID).child("startTime").getValue(String.class));
                    event.setStartDate(dataSnapshot.child(eventID).child("startDate").getValue(String.class));
                    event.setEndTime(dataSnapshot.child(eventID).child("endTime").getValue(String.class));
                    event.setEndDate(dataSnapshot.child(eventID).child("endDate").getValue(String.class));
                    event.setPrice(dataSnapshot.child(eventID).child("price").getValue(String.class));
                    event.setLocation(dataSnapshot.child(eventID).child("location").getValue(String.class));
                    event.setRegisterLink(dataSnapshot.child(eventID).child("registerLink").getValue(String.class));
                    events.add(event);
                }
                TimelineEventAdapter eventAdapter = new TimelineEventAdapter();

                RecyclerView recyclerView = findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                eventAdapter.updateData(events);
                eventAdapter.setOnItemClickListener((event) -> {
                    Intent intent = new Intent(TripDetailsActivity.this, EventDetailsActivity.class);
                    intent.putExtra("event", event);
                    startActivity(intent);
                    finish();
                });
                recyclerView.setAdapter(eventAdapter);
            }
        }).addOnFailureListener(e -> {
            Log.e("EventRepository", "Error retrieving event data: " + e.getMessage());
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TripDetailsActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}