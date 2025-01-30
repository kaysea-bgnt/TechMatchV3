package appdev.com.techmatch.service;

import org.springframework.stereotype.Service;
import appdev.com.techmatch.model.Event;
import appdev.com.techmatch.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.*;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public Event saveEvent(Event event) {
        // Only generate an event ID if it's a new event (i.e., eventID is null)
        if (event.getEventID() == null || event.getEventID().isEmpty()) {
            event.setEventID(generateCustomEventID());
            return eventRepository.save(event);
        }
    
        // If the event already exists, fetch it from the database and update it instead
        Event existingEvent = eventRepository.findById(event.getEventID()).orElse(null);
        if (existingEvent == null) {
            throw new IllegalArgumentException("Event not found: " + event.getEventID());
        }
    
        // Only update attendees, do NOT overwrite the event ID
        existingEvent.setAttendees(event.getAttendees());
    
        return eventRepository.save(existingEvent);
    }
    

    private String generateCustomEventID() {
        long count = eventRepository.count();
        return String.format("EVENT-%05d", count + 1);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(String eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> 
            new RuntimeException("Event not found with ID: " + eventId));
    }

    public List<Event> getEventsByTopic(String topic) {
        List<Event> events = eventRepository.findByTopics_Name(topic);
        System.out.println("Events found for topic " + topic + ": " + events.size());
        return events;
    }
    
    public List<Event> getEventsByDateAndType(String date, String type) {
        List<Event> events;
        if (date != null && type != null) {
            events = eventRepository.findByDateAndType(LocalDate.parse(date), type);
        } else if (date != null) {
            events = eventRepository.findByStartDate(LocalDate.parse(date));
        } else {
            events = eventRepository.findByEventType(type);
        }
        System.out.println("Events found for date " + date + " and type " + type + ": " + events.size());
        return events;
    }

    public List<Event> getEventsByUserID(String userID) {
        return eventRepository.findByUserUserID(userID);
    }
    

    
    
}