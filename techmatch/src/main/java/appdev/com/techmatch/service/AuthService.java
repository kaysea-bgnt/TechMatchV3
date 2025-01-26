package appdev.com.techmatch.service;

import appdev.com.techmatch.model.User;
import appdev.com.techmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User authenticate(String usernameOrEmail , String password) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    
}
