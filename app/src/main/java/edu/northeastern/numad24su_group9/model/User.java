package edu.northeastern.numad24su_group9.model;

import androidx.annotation.NonNull;

import java.util.List;

public class User {
    private String userID;
    private String name;
    private List<String> plannedTrips; // list of trip IDs
    private String profileImage;
    private List<String> eventsAttended; // list of event IDs
    private List<String> interests; // list of interests

    public User(){}

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTrips() {
        return plannedTrips;
    }

    public void setTrips(List<String> plannedTrips) {
        this.plannedTrips = plannedTrips;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public List<String> getEventsAttended() {
        return eventsAttended;
    }

    public void setEventsAttended(List<String> eventsAttended) {
        this.eventsAttended = eventsAttended;
    }

    @NonNull
    @Override
    public String toString() {
        return "User ID: " + userID + "Name: " + name + "Planned Trips: " + plannedTrips + "Profile Image: " + profileImage + "Events Attended: " + eventsAttended + "Interests: " + interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }
}
