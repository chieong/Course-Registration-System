package org.cityuhk.CourseRegistrationSystem.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManageCourseController {

    @GetMapping("/ManageCourse") 
    public String manageCourse(Model model) { 
        return "ManageCourse";
    }

}
