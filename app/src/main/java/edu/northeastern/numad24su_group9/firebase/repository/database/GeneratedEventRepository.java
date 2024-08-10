package edu.northeastern.numad24su_group9.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.numad24su_group9.firebase.DatabaseConnector;

public class GeneratedEventRepository {
    private final DatabaseReference eventRef;

    public GeneratedEventRepository() {
        eventRef = DatabaseConnector.getInstance().getGeneratedEventsReference();
    }

    public DatabaseReference getGeneratedEventRef() {
        return eventRef;
    }
}
