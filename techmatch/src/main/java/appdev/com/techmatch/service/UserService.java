package appdev.com.techmatch.service;

import org.springframework.stereotype.Service;
import appdev.com.techmatch.model.User;
import appdev.com.techmatch.repository.UserRepository;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public boolean isUsernameAlreadyInUse(String username) {
        return userRepository.findByUsernameOrEmail(username, null) != null;
    }

    // If Email or Username Exist
    public boolean isEmailAlreadyInUse(String email) {
        return userRepository.findByUsernameOrEmail(null, email) != null;
    }

    public User saveUser(User user) {
        user.setUserID(generateCustomUserID());
        return userRepository.save(user);
    }

    private String generateCustomUserID() {
        //long count = userRepository.count();
        //return String.format("USER-%06d", count + 1);
        String lastUserID = userRepository.findMaxUserID();
        if (lastUserID == null) {
            return "USER-000001";
        }

        // Extract numeric part
        int lastNumber = Integer.parseInt(lastUserID.replace("USER-", ""));
        return String.format("USER-%06d", lastNumber + 1);
    }

    public User getUserById(String userID) {
        Optional<User> user = userRepository.findById(userID);
        return user.orElse(null);
    }

    
}
