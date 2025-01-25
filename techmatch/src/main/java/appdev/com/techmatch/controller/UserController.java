package appdev.com.techmatch.controller;
import appdev.com.techmatch.model.User;
import appdev.com.techmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public String signUp(@ModelAttribute User user) {
        userService.saveUser(user);
        return "redirect:/login";
    }
    
}
