package appdev.com.techmatch.controller;

import appdev.com.techmatch.model.Event;
import appdev.com.techmatch.model.User;
import appdev.com.techmatch.repository.EventRepository;
import appdev.com.techmatch.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import appdev.com.techmatch.service.EventService;

import java.time.LocalDate;
import java.util.*;

@Controller
public class LoginController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventService eventService;

    @GetMapping("/login")
    public String loginPage() {
        return "index";
    }

    @PostMapping("/login")
    public String handleLogin(
        @RequestParam("usernameOrEmail") String usernameOrEmail,
        @RequestParam("password") String password,
        Model model,
        HttpSession session
    ) {
        User user = authService.authenticate(usernameOrEmail, password);
        if (user != null) {
            session.setAttribute("loggedInUser", user); //store the user in the session
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Invalid username/email or password");
            return "index";
        }
    }

    @GetMapping("/home")
    public String showHomePage( HttpSession session, Model model) {
        //check if the user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();

        //fetch all events
        List<Event> events;
        try {
             events = eventRepository.findByEndDateAfterOrEndDateEquals(today);
        } catch (Exception e) {
            events = Collections.emptyList(); // Use an empty list if an error occurs
            System.err.println("Error fetching events: " + e.getMessage());
        }
    
        model.addAttribute("events", events);
        model.addAttribute("email", loggedInUser.getEmail());
        model.addAttribute("username", loggedInUser.getUsername());
        model.addAttribute("userID", loggedInUser.getUserID());
        return "home";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear all session data
        return "redirect:/login?logout=true"; // Redirect to login with a logout flag
    }
    

    @GetMapping("/user/details")
    @ResponseBody
    public Map<String, Object> getUserDetails(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        Map<String, Object> userResponse = new HashMap<>();

        if (loggedInUser != null) {
            userResponse.put("userID", loggedInUser.getUserID());
            userResponse.put("username", loggedInUser.getUsername());
            userResponse.put("email", loggedInUser.getEmail());
        }

        return userResponse;
    }

    @GetMapping("/create")
    public String createEventPage() {
        return "create-event";
    }

    // PASS LOGGED USER DETAILS
      // removed home before profile, so url is /profile
     /* 
     public String profilePage(HttpSession session, Model model) {
         User loggedInUser = (User) session.getAttribute("loggedInUser");
         if (loggedInUser == null) {
            System.out.println("No logged-in user found.");
             return "redirect:/login"; // Redirect to login if not authenticated
         }
         // Pass the user details to the profile page
        model.addAttribute("user", loggedInUser);
        return "profile"; // Return the profile view 
        }

        
        @GetMapping("/profile")
        public String showprofile( HttpSession session, Model model) {
            //check if the user is logged in
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                return "redirect:/login";
            }
    
            //fetch all events
            List<Event> events;
            try {
                events = eventService.getAllEvents();
            } catch (Exception e) {
                events = Collections.emptyList(); // Use an empty list if an error occurs
                System.err.println("Error fetching events: " + e.getMessage());
            }
        
            model.addAttribute("events", events);
            model.addAttribute("email", loggedInUser.getEmail());
            model.addAttribute("username", loggedInUser.getUsername());
            model.addAttribute("userID", loggedInUser.getUserID());
            return "profile";
        }
        */

        @GetMapping("/profile")
        public String profile(Model model, HttpSession session) {
            //CHECK IF USER IS LOGGED IN
            User loggedInUser = (User) session.getAttribute("loggedInUser");
                if (loggedInUser == null) {
                    return "redirect:/login";
            }
       
            // Get all events a user is registered for
            List<Event> registeredEvents = eventRepository.findRegisteredEventsByUserId(loggedInUser.getUserID());

            // Filter events into upcoming and past
            List<Event> upcomingEvents = eventService.filterUpcomingEvents(registeredEvents);
            List<Event> pastEvents = eventService.filterPastEvents(registeredEvents);

            model.addAttribute("username", loggedInUser.getUsername());
            model.addAttribute("email", loggedInUser.getEmail());
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("pastEvents", pastEvents);
            return "profile";
        }

}