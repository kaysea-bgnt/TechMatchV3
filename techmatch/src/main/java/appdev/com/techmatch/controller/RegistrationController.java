package appdev.com.techmatch.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import appdev.com.techmatch.service.RegistrationService;
import appdev.com.techmatch.dto.RegistrationRequest;
import appdev.com.techmatch.repository.RegistrationRepository;
import appdev.com.techmatch.model.Registration;
import java.util.List;

@RestController
@RequestMapping("/register")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody RegistrationRequest request) {
        try {
            registrationService.registerUser(request.getUserID(), request.getEventID());
            return ResponseEntity.ok("Registered successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
    private RegistrationRepository registrationRepository;

    @GetMapping("/event/{eventID}")
    public List<Registration> getRegistrationsForEvent(@PathVariable String eventID) {
        return registrationRepository.findByEventID(eventID);
    }
}