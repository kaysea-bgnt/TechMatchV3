package appdev.com.techmatch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {


    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; 
    }


    
    

}
