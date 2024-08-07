package edu.northeastern.numad24su_group9.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Event implements Serializable {
    private String title;
    private String startTime;
    private String endTime;
    private String startDate;
    private String endDate;
    private String description;
    private String price;
    private String location;
    private String registerLink;
    private String image;
    private String eventID;
    private String createdBy;

    public Event() {}

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRegisterLink(String registerLink) {
        this.registerLink = registerLink;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public String getRegisterLink() {
        return registerLink;
    }

    public String getEventID() {
        return eventID;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isWithinDateRange(String startDate, String endDate) {

        // Parse the date strings into LocalDate objects
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate eventDate = LocalDate.parse(this.startDate, formatter);
        LocalDate tripStartDate = LocalDate.parse(startDate, formatter);
        LocalDate tripEndDate = LocalDate.parse(endDate, formatter);

        // Compare the dates
        int resultAfterOrEqualStart = eventDate.compareTo(tripStartDate);
        int resultAfterOrEqualEnd = eventDate.compareTo(tripEndDate);

        return (resultAfterOrEqualStart >= 0) && (resultAfterOrEqualEnd <= 0);
    }

    @NonNull
    @Override
    public String toString() {
        return "Title: " + title + "Description: " + description + "Image: " + image + "Start Time: " + startTime + "End Time: " + endTime + "Start Date: " + startDate + "End Date: " + endDate + "Price: " + price + "Location: " + location;
    }
}

