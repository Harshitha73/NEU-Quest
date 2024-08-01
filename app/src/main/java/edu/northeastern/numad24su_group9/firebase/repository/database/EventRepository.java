package edu.northeastern.numad24su_group9.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.numad24su_group9.firebase.DatabaseConnector;

public class EventRepository {
    private final DatabaseReference eventRef;

    public EventRepository() {
        eventRef = DatabaseConnector.getInstance().getEventsReference();
    }

    public DatabaseReference getEventRef() {
        return eventRef;
    }
}
