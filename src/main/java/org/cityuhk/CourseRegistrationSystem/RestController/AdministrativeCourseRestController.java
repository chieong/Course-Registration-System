package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdministrativeCourseRestController {

    private final AdministrativeService administrativeService;
    private final CourseService courseService;

    public AdministrativeCourseRestController(AdministrativeService administrativeService,
                                              CourseService courseService) {
        this.administrativeService = administrativeService;
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    public ResponseEntity<?> listCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @PostMapping("courses")
    public ResponseEntity<Course> createCourse(@RequestBody AdminCourseRequest request) {
        Course created = administrativeService.createCourse(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("course")
    public ResponseEntity<Course> modifyCourse(@RequestBody AdminCourseRequest request) {
        Course update = administrativeService.modifyCourse(request);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("course/{courseCode}")
    public ResponseEntity<Void> removeCourse(@PathVariable String courseCode) {
        administrativeService.removeCourse(courseCode);
        return  ResponseEntity.noContent().build();
    }
}
