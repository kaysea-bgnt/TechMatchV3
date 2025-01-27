package appdev.com.techmatch.controller;
import appdev.com.techmatch.model.User;
import appdev.com.techmatch.repository.UserRepository;
import appdev.com.techmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;


@Controller
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/signup")
public String signUp(@ModelAttribute User user, Model model) {
    String email = user.getEmail().trim().toLowerCase();

    // Email domain validation
    if (!email.endsWith("@iskolarngbayan.pup.edu.ph")) {
        model.addAttribute("error", "Invalid email. Please use your PUP webmail.");
        return "signup";
    }

    // General email format validation
    if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
        model.addAttribute("error", "Invalid email format.");
        return "signup";
    }

    // Check if the username already exists
    if (userService.isUsernameAlreadyInUse(user.getUsername())) {
        model.addAttribute("error", "Invalid username.");
        model.addAttribute("user", user);  // Re-populate the user object with the submitted values
        return "signup"; // Redirect back to signup page with error
    }

    // Check if email already exists
    if (userRepository.findByUsernameOrEmail(null, email) != null) {
        model.addAttribute("error", "Email Already in Use.");
        model.addAttribute("user", user);
        return "signup";
    }


    // Save user
    user.setEmail(email); // Ensure standardized email format
    userService.saveUser(user);

    return "redirect:/login";
    }

    
}


