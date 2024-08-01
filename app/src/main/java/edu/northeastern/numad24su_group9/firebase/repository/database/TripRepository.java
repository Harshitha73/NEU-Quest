package edu.northeastern.numad24su_group9.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.numad24su_group9.firebase.DatabaseConnector;

public class TripRepository {
    private final DatabaseReference tripRef;

    public TripRepository() {
        tripRef = DatabaseConnector.getInstance().getTripsReference();
    }

    public DatabaseReference getTripRef() {
        return tripRef;
    }
}
