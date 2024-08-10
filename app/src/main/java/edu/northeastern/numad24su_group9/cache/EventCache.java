package edu.northeastern.numad24su_group9.cache;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad24su_group9.model.Event;

public class EventCache {
    private static EventCache instance;
    private final List<Event> cachedEvents;

    private EventCache() {
        cachedEvents = new ArrayList<>();
    }

    public static EventCache getInstance() {
        if (instance == null) {
            instance = new EventCache();
        }
        return instance;
    }

    public void addEvents(List<Event> events) {
        cachedEvents.addAll(events);
    }

    public List<Event> getCachedEvents() {
        return cachedEvents;
    }

    public void clearCache() {
        cachedEvents.clear();
    }
}
