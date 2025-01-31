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
import jakarta.servlet.http.HttpSession;


import appdev.com.techmatch.repository.TopicRepository;
import appdev.com.techmatch.service.UserService;
import appdev.com.techmatch.repository.EventRepository;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
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
        @RequestParam("isFree") String isFreeStr, //capture the isFree value as string
        HttpSession session //get session
    ) throws IOException {

        event.setFree(Boolean.parseBoolean(isFreeStr));
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        String userID = loggedInUser.getUserID(); // Get user ID from the User object
        User user = userService.getUserById(userID);
        
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
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
        response.put("user", event.getUser());

        response.put("topics", event.getTopics().stream()
        .map(topic -> {
            Map<String, Object> topicDetails = new HashMap<>();
            topicDetails.put("id", topic.getTopicID());  // Now getId() should work
            topicDetails.put("name", topic.getName());
            return topicDetails;
        })
        .collect(Collectors.toList()));
    
        response.put("organization", event.getOrganization());
        response.put("startTime", event.getStartTime());
        response.put("endTime", event.getEndTime());
        response.put("eventImage", Base64.getEncoder().encodeToString(event.getEventImage()));
        response.put("isFree", event.isFree());
        response.put("capacity", event.getCapacity());

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
        @RequestParam(value = "topic", required = false) String[] topics,
        @RequestParam(value = "eventType", required = false) String eventType,
          @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "startDate", required = false) String startDate,
        @RequestParam(value = "endDate", required = false) String endDate,
        Model model
    ) {
        List<Event> events;
         if (startDate != null && endDate != null && topics != null && topics.length > 0 && eventType != null && !eventType.isEmpty()) {
          events = eventService.getEventsByTopicsAndDateAndType(Arrays.asList(topics), startDate, eventType);
        }
        else if (startDate != null && endDate != null && topics != null && topics.length > 0) {
          events = eventService.getEventsByTopicsAndDate(Arrays.asList(topics), startDate);
         }
        else if (startDate != null && endDate != null && eventType != null && !eventType.isEmpty()) {
             events = eventService.getEventsByDateAndType(startDate, eventType);
        }
         else if (startDate != null && endDate != null) {
          events = eventService.getEventsByDateRange(startDate, endDate);
        }
        else if (searchQuery != null && !searchQuery.isEmpty()) {
             events = eventService.searchEvents(searchQuery);
        }
        else if (topics != null && topics.length > 0 && eventType != null && !eventType.isEmpty()) {
             events = eventService.getEventsByTopicsAndEventType(Arrays.asList(topics), eventType);
        }
        else if (topics != null && topics.length > 0) {
            events = eventService.getEventsByTopics(Arrays.asList(topics));
        }
         else if (eventType != null && !eventType.isEmpty()) {
            events = eventService.getEventsByEventType(eventType);
        }
         else {
            events = eventService.getAllEvents();
        }
        model.addAttribute("events", events);
        return "home"; // Return view name
    }




    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<String> registerForEvent(@RequestBody Map<String, String> request) {
        String userID = request.get("userID");
        String eventID = request.get("eventID");

        if (userID == null || eventID == null || userID.isEmpty() || eventID.isEmpty()) {
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

        // Prevent the event creator from registering
        if (event.getUser().getUserID().equals(userID)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cannot register for your own event.");
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

    @GetMapping("/my-events")
    public String getUserCreatedEvents(HttpSession session, Model model) {
        // Get the logged-in user's ID from session
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        if (loggedInUser == null) {
            return "redirect:/login"; // Redirect if user is not logged in
        }
    
        // Fetch events created by this user
        List<Event> userEvents = eventService.getEventsByUserID(loggedInUser.getUserID());
        
        model.addAttribute("userEvents", userEvents);
        
        return "my-events"; // This will be your HTML page displaying user-created events
    }

    @DeleteMapping("/delete/{eventID}")
    @ResponseBody
    public ResponseEntity<String> deleteEvent(@PathVariable String eventID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
    
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in.");
        }
    
        Event event = eventService.getEventById(eventID);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
        }
    
        // Ensure only the creator can delete the event
        if (!event.getUser().getUserID().equals(loggedInUser.getUserID())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this event.");
        }
    
        eventService.deleteEvent(eventID);
        return ResponseEntity.ok("Event deleted successfully.");
    } 

    @GetMapping("/edit/{eventID}")
    public String editEvent(@PathVariable String eventID, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Redirect if not logged in
        }
    
        Event event = eventService.getEventById(eventID);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
    
        // Ensure only the creator can edit
        if (!event.getUser().getUserID().equals(loggedInUser.getUserID())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to edit this event.");
        }

        List<Topic> allTopics = topicRepository.findAll(); // Get all available topics
    
        model.addAttribute("event", event);
        model.addAttribute("allTopics", allTopics);
        return "edit-event";
    }

    @PostMapping("/update")
    public String updateEvent(
        @ModelAttribute Event updatedEvent,
        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
        @RequestParam("eventTopics") List<String> eventTopics,
        HttpSession session ) throws IOException {
    User loggedInUser = (User) session.getAttribute("loggedInUser");

    if (loggedInUser == null) {
        return "redirect:/login"; // Redirect if not logged in
    }

    Event existingEvent = eventService.getEventById(updatedEvent.getEventID());
    if (existingEvent == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
    }

    // Ensure only the event creator can update it
    if (!existingEvent.getUser().getUserID().equals(loggedInUser.getUserID())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this event.");
    }

    // Update the event fields
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
    existingEvent.setEventType(updatedEvent.getEventType());

    // Update topics
    Set<Topic> updatedTopics = new HashSet<>();
    for (String topicName : eventTopics) {
        Topic topic = topicRepository.findByName(topicName);
        if (topic == null) {
            topic = new Topic();
            topic.setName(topicName);
            topicRepository.save(topic);
        }
        updatedTopics.add(topic);
    }
    existingEvent.setTopics(updatedTopics);

    // Handle image upload
    if (imageFile != null && !imageFile.isEmpty()) {
        existingEvent.setEventImage(imageFile.getBytes());
    }

    eventService.saveEvent(existingEvent); // Save the updated event

    return "redirect:/events/my-events"; // Redirect back to user events page
}

@DeleteMapping("/attendees/delete")
@ResponseBody
public ResponseEntity<String> deleteAttendee(
        @RequestParam String userId, 
        @RequestParam String eventId, 
        HttpSession session) {
    
    User loggedInUser = (User) session.getAttribute("loggedInUser");

    if (loggedInUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in.");
    }

    Event event = eventService.getEventById(eventId);
    if (event == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
    }

    // Ensure only the event creator can remove attendees
    if (!event.getUser().getUserID().equals(loggedInUser.getUserID())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to remove attendees.");
    }

    boolean removed = eventService.removeAttendee(eventId, userId);
    if (removed) {
        return ResponseEntity.ok("Attendee removed successfully.");
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attendee not found.");
    }
}


    
    
    

    

}