package appdev.com.techmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import appdev.com.techmatch.service.EventService;
import org.springframework.ui.Model;
import java.util.*;
import appdev.com.techmatch.model.Event;

@Controller
public class PageController {

    @Autowired
    private EventService eventService;

    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; 
    }

    @GetMapping("/login")
    public String loginPage() {
        return "index";
    }

    @GetMapping("/home")
    public String showHomePage(Model model) {
        List<Event> events;
        try {
            events = eventService.getAllEvents();
        } catch (Exception e) {
            events = Collections.emptyList(); // Use an empty list if an error occurs
            System.err.println("Error fetching events: " + e.getMessage());
        }
    
        model.addAttribute("events", events);
        return "home";
    }
    
    @GetMapping("/create")
    public String createEventPage() {
        return "create-event";
    }
}
