package appdev.com.techmatch.controller;

import appdev.com.techmatch.model.Event;
import appdev.com.techmatch.model.User;
import appdev.com.techmatch.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import appdev.com.techmatch.service.EventService;
import java.util.*;

@Controller
public class LoginController {

    @Autowired
    private AuthService authService;

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
        return "home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); //clear the session
        return "redirect:/login";
    }
    
}
