package org.cityuhk.course_registration_system;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class studentlistController {

    @GetMapping("/studentlist") 
    public String studentlist(Model model) { 
        return "studentlist";
    }

}
