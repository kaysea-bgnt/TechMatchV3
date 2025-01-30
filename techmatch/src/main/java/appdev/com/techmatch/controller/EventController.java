package appdev.com.techmatch.controller;

import appdev.com.techmatch.dto.EventAttendeeDTO;
import appdev.com.techmatch.model.Event;
import appdev.com.techmatch.model.Topic;
import appdev.com.techmatch.service.EventService;
import appdev.com.techmatch.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import appdev.com.techmatch.repository.TopicRepository;
import appdev.com.techmatch.service.UserService;
import appdev.com.techmatch.repository.EventRepository;

import java.io.IOException;
import java.util.*;

import org.springframework.http.ResponseEntity;



@Controller
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EventRepository eventRepository;

    @PostMapping("/create")
    public String createEvent(
        @ModelAttribute Event event,
        @RequestParam("eventTopics") String[] eventTopics, // Collect selected topics
        @RequestParam("imageFile") MultipartFile imageFile,
        @RequestParam("userID") String userID,
        @RequestParam(value = "isFree", defaultValue = "false") boolean isFree // Fix here
    ) throws IOException {
        // Combine selected topics into a comma-separated string
        
        // Link the user to the event
        User user = new User();
        user.setUserID(userID);
        event.setUser(user);
    
        // Process the uploaded image
        if (!imageFile.isEmpty()) {
            event.setEventImage(imageFile.getBytes());
        }


        // Process event topics
        for (String topicName : eventTopics) {
            // Check if topic already exists
            Topic topic = topicRepository.findByName(topicName);
            if (topic == null) {
                // Create a new topic if it doesn't exist
                topic = new Topic();
                topic.setName(topicName);
                topicRepository.save(topic);
            }
            event.getTopics().add(topic);
        }

        eventService.saveEvent(event);
    
    
        return "redirect:/home";
    }

@GetMapping("/{id}") // Change from /events/{id} to simply /{id}
@ResponseBody
public Map<String, Object> getEventDetails(@PathVariable String id) {
    Event event = eventService.getEventById(id);

    if (event == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
    }

    Map<String, Object> response = new HashMap<>();
    response.put("eventName", event.getEventName());
    response.put("description", event.getDescription());
    response.put("location", event.getLocation());
    response.put("startDate", event.getStartDate());
    response.put("endDate", event.getEndDate());
    response.put("eventType", event.getEventType());
    response.put("topics", event.getTopics().stream().map(Topic::getName).toList());
    response.put("organization", event.getOrganization());
    response.put("startTime", event.getStartTime());
    response.put("endTime", event.getEndTime());
    response.put("eventImage", Base64.getEncoder().encodeToString(event.getEventImage()));
    response.put("isFree", event.isFree());

    return response;
}

    

    @GetMapping("/create")
    public String showEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "create-event"; // Render the event creation form
    }


    // Updated method for topic-based filtering
    @GetMapping
    public String getFilteredEvents(
        @RequestParam(value = "topic", required = false) String topic,
        Model model
    ) {
        List<Event> events;

        if (topic != null && !topic.isEmpty()) {
            events = eventService.getEventsByTopic(topic);
        } else {
            events = eventService.getAllEvents();
        }

        model.addAttribute("events", events);
        model.addAttribute("selectedTopic", topic); // To highlight the selected topic in the UI
        return "home"; // Render the home.html template with filtered events
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<String> registerForEvent(@RequestBody Map<String, String> request) {
        String userID = request.get("userID");
        String eventID = request.get("eventID");
    
        if (userID == null || eventID == null || userID.isEmpty() || eventID.isEmpty()) {
            System.out.println("❌ ERROR: Missing or empty userID or eventID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or empty userID or eventID");
        }
    
        Event event = eventService.getEventById(eventID);
        if (event == null) {
            System.out.println("❌ ERROR: Event not found - " + eventID);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }
    
        User user = userService.getUserById(userID);
        if (user == null) {
            System.out.println("❌ ERROR: User not found - " + userID);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    
        // Check if the user is already registered
        if (event.getAttendees().contains(user)) {
            System.out.println("❌ ERROR: User already registered for event - " + eventID);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already registered for this event.");
        }
    
        // Add user to event attendees
        event.getAttendees().add(user);
    
        // Save the event
        eventService.saveEvent(event);
    
        return ResponseEntity.ok("User registered successfully");
    }

    
    
    
    @GetMapping("/attendees/{eventID}")
    public String getEventAttendees(@PathVariable String eventID, Model model) {
        List<EventAttendeeDTO> attendees = eventRepository.getEventAttendeesWithDetails(eventID);
        model.addAttribute("attendees", attendees);
        return "event_attendees"; 
    }
    
        
    

    
}