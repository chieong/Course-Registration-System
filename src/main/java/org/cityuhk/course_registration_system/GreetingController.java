package org.cityuhk.course_registration_system;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {

    @GetMapping("/greeting") 
    // ensures that HTTP GET requests to /greeting are mapped to the greeting() method
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) { 
        // @RequestParam binds the value of the query string parameter name into the name parameter of the greeting() method
        // this query string parameter is not required. if it is absent in the request, the defaultValue of World is used.
        model.addAttribute("name", name);
        return "greeting";
    }

}
