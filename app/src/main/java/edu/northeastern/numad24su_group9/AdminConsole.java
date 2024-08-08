package edu.northeastern.numad24su_group9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.recycler.AdminConsoleAdapter;
import edu.northeastern.numad24su_group9.recycler.EventAdapter;

public class AdminConsole extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminConsoleAdapter adapter;
    private List<Event> eventList;
    private ArrayList<Event> allEvents;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_console);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressBar = findViewById(R.id.AdminConsoleProgressBar);
        getEvents();
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
        adapter = new AdminConsoleAdapter(this);
        adapter.updateData(events);
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}