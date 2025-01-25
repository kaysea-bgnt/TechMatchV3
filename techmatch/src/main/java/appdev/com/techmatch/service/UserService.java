package appdev.com.techmatch.service;

import org.springframework.stereotype.Service;
import appdev.com.techmatch.model.User;
import appdev.com.techmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        user.setUserID(generateCustomUserID());
        return userRepository.save(user);
    }

    private String generateCustomUserID() {
        long count = userRepository.count();
        return String.format("USER-%06d", count + 1);
    }
}
