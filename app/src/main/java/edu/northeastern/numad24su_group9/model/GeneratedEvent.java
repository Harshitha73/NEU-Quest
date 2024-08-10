package edu.northeastern.numad24su_group9.model;

public class GeneratedEvent {
    private String title;
    private String description;
    private String id;

    // Default constructor
    public GeneratedEvent() {
    }

    // Constructors, getters, and setters
    public GeneratedEvent(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GeneratedEvent{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id='" + id + '\'';
    }
}
