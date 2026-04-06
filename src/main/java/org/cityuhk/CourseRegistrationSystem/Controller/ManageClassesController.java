package org.cityuhk.CourseRegistrationSystem.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManageClassesController {

    @GetMapping("/manageclass")
    public String manageClasses(Model model) {
        return "manageclass"; // resolves to templates/manage-classes.html
    }
}
