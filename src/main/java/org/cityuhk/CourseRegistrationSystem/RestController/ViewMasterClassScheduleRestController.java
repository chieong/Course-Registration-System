package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.SectionService;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class ViewMasterClassScheduleRestController {
    private final SectionService sectionService;
    private final CourseService courseService;

    @Autowired
    public ViewMasterClassScheduleRestController(SectionService sectionService, CourseService courseService) {
        this.sectionService = sectionService;
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllSections() {
        return ResponseEntity.ok(courseService.getAllCourses());// TODO CHANGE IT BACK!!!!!!!!!!!!!!
    }


}

