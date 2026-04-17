package org.cityuhk.CourseRegistrationSystem.Controller;

import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdministrativeCourseRestController {

    private final AdministrativeService administrativeService;

    public AdministrativeCourseRestController(AdministrativeService administrativeService) {
        this.administrativeService = administrativeService;
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody AdminCourseRequest request) {
        try {
            Course created = administrativeService.createCourse(request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{courseCode}")
    public ResponseEntity<?> modifyCourse(@PathVariable String courseCode, @RequestBody AdminCourseRequest request) {
        try {
            Course updated = administrativeService.modifyCourse(courseCode, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{courseCode}")
    public ResponseEntity<?> removeCourse(@PathVariable String courseCode) {
        try {
            administrativeService.removeCourse(courseCode);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
