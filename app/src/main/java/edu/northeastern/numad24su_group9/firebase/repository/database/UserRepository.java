package edu.northeastern.numad24su_group9.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.numad24su_group9.firebase.DatabaseConnector;

public class UserRepository {
    private final DatabaseReference userRef;

    public UserRepository(String userId) {
        userRef = DatabaseConnector.getInstance().getUsersReference(userId);
    }

    public DatabaseReference getUserRef() {
        return userRef;
    }
}
