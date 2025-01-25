package appdev.com.techmatch.service;

import appdev.com.techmatch.model.Registration;
import appdev.com.techmatch.model.User;
import appdev.com.techmatch.repository.RegistrationRepository;
import appdev.com.techmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RegistrationService {
    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    public void registerUser(String userID, String eventID) {
        // Check if the user is already registered for the event
        if (registrationRepository.existsByUserIDAndEventID(userID, eventID)) {
            throw new RuntimeException("User is already registered for this event");
        }

        // Fetch user details
        User user = userRepository.findById(userID).orElseThrow(() ->
                new RuntimeException("User not found with ID: " + userID));

        // Create a new registration record
        Registration registration = new Registration();
        registration.setUserID(user.getUserID());
        registration.setEventID(eventID);
        registration.setName(user.getUsername());
        registration.setEmail(user.getEmail());

        registrationRepository.save(registration);
    }
}

