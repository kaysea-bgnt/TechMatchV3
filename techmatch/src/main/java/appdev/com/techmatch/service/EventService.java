package appdev.com.techmatch.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import appdev.com.techmatch.model.Event;
import appdev.com.techmatch.repository.EventRepository;
import jakarta.transaction.Transactional;
import appdev.com.techmatch.model.User;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserService userService;

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

    public List<Event> getEventsByEventType(String type) {
        return eventRepository.findByEventType(type);
    }

    public List<Event> getEventsByTopics(List<String> topics) {
        List<Event> events = eventRepository.findByTopics_NameIn(topics);
        System.out.println("Events found for topics " + topics + ": " + events.size());
        return events;
    }

    public List<Event> getEventsByTopicsAndEventType(List<String> topics, String eventType) {
        return eventRepository.findByTopicsNameInAndEventType(topics, eventType);
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

    @Transactional
    public void deleteEvent(String eventID) {
        eventRepository.deleteById(eventID);
    }
    

    public List<Event> searchEvents(String searchQuery) {
        return eventRepository.findByEventNameContainingIgnoreCaseOrOrganizationContainingIgnoreCase(searchQuery, searchQuery);
    }
    
    public Event updateEvent(Event updatedEvent, MultipartFile imageFile) throws IOException {
        Event existingEvent = eventRepository.findById(updatedEvent.getEventID())
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    
        // Update fields
        existingEvent.setEventName(updatedEvent.getEventName());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setStartDate(updatedEvent.getStartDate());
        existingEvent.setEndDate(updatedEvent.getEndDate());
        existingEvent.setStartTime(updatedEvent.getStartTime());
        existingEvent.setEndTime(updatedEvent.getEndTime());
        existingEvent.setOrganization(updatedEvent.getOrganization());
        existingEvent.setFree(updatedEvent.isFree());
        existingEvent.setCapacity(updatedEvent.getCapacity());
    
        // If a new image is provided, update it
        if (imageFile != null && !imageFile.isEmpty()) {
            existingEvent.setEventImage(imageFile.getBytes());
        }
    
        return eventRepository.save(existingEvent);
    }

    @Transactional
    public boolean removeAttendee(String eventID, String userID) {
        Event event = eventRepository.findById(eventID).orElse(null);
        if (event == null) return false;
    
        User user = userService.getUserById(userID);
        if (user == null) return false;
    
        if (!event.getAttendees().contains(user)) return false;
    
        event.getAttendees().remove(user);
        eventRepository.save(event); // Update the event
    
        return true;
    }
    

    
    // CALENDAR FILTER
    public List<Event> getEventsByDateRange(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return eventRepository.findByStartDateBetween(start, end); // Modified line
    }

    public List<Event> getEventsByTopicsAndDate(List<String> topics, String date) {
        List<Event> events = eventRepository.findByTopicsNameInAndStartDate(topics, LocalDate.parse(date));
          System.out.println("Events found for topics " + topics + " and date " + date + ": " + events.size());
        return events;
    }

    public List<Event> getEventsByTopicsAndDateAndType(List<String> topics, String date, String eventType) {
        List<Event> events = eventRepository.findByTopicsNameInAndStartDateAndEventType(topics, LocalDate.parse(date), eventType);
        System.out.println("Events found for topics " + topics + " and date " + date + " and type " + eventType +": " + events.size());
        return events;
    }


    public  List<Event> filterUpcomingEvents(List<Event> registeredEvents) {
        LocalDateTime now = LocalDateTime.now();
        return registeredEvents.stream()
                .filter(event -> {
                     if(event == null || event.getStartDate() == null) return false;
                     LocalDateTime eventDateTime;
                        if (event.getStartTime() != null) {
                            eventDateTime = LocalDateTime.of(event.getStartDate(), event.getStartTime());
                         }else{
                            eventDateTime = LocalDateTime.of(event.getStartDate(), LocalTime.MIDNIGHT);
                        }
                     LocalDateTime endDateTime;
                     if(event.getEndDate() != null){
                         if(event.getEndTime() != null){
                            endDateTime = LocalDateTime.of(event.getEndDate(), event.getEndTime());
                        }else{
                             endDateTime = LocalDateTime.of(event.getEndDate(), LocalTime.MIDNIGHT);
                        }
                     }
                     else{
                       endDateTime = eventDateTime;
                     }
                    return endDateTime.isAfter(now);
                })
                .collect(Collectors.toList());
    }

public  List<Event> filterPastEvents(List<Event> registeredEvents) {
        LocalDateTime now = LocalDateTime.now();
       return registeredEvents.stream()
                .filter(event -> {
                     if(event == null || event.getStartDate() == null) return false;
                     LocalDateTime eventDateTime;
                    if (event.getStartTime() != null) {
                       eventDateTime = LocalDateTime.of(event.getStartDate(), event.getStartTime());
                    }else{
                        eventDateTime = LocalDateTime.of(event.getStartDate(), LocalTime.MIDNIGHT);
                    }
                        LocalDateTime endDateTime;
                        if(event.getEndDate() != null){
                            if(event.getEndTime() != null){
                                endDateTime = LocalDateTime.of(event.getEndDate(), event.getEndTime());
                            }else{
                               endDateTime = LocalDateTime.of(event.getEndDate(), LocalTime.MIDNIGHT);
                            }
                       }
                        else{
                          endDateTime = eventDateTime;
                        }

                   return endDateTime.isBefore(now);
                })
             .collect(Collectors.toList());
}








    

}