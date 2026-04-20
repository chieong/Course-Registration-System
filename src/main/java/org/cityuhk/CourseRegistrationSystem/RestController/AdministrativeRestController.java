package org.cityuhk.CourseRegistrationSystem.RestController;

import java.util.List;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.InstructorUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.StudentUserRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdministrativeRestController {

    private final AdministrativeService administrativeService;

    @Autowired
    public AdministrativeRestController(AdministrativeService administrativeService) {
        this.administrativeService = administrativeService;
    }

    // ── Admin user endpoints ───────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<Admin>> listUsers() {
        return ResponseEntity.ok(administrativeService.listUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AdminUserRequest request) {
        try {
            Admin created = administrativeService.createUser(request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/users/{staffId}")
    public ResponseEntity<?> modifyUser(@PathVariable Integer staffId, @RequestBody AdminUserRequest request) {
        try {
            Admin updated = administrativeService.modifyUser(staffId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{staffId}")
    public ResponseEntity<?> removeUser(@PathVariable Integer staffId) {
        try {
            administrativeService.removeUser(staffId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ── Student user endpoints ─────────────────────────────────────────────────

    @GetMapping("/students")
    public ResponseEntity<List<Student>> listStudents() {
        return ResponseEntity.ok(administrativeService.listStudents());
    }

    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@RequestBody StudentUserRequest request) {
        try {
            Student created = administrativeService.createStudent(request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/students/{studentId}")
    public ResponseEntity<?> modifyStudent(@PathVariable Integer studentId, @RequestBody StudentUserRequest request) {
        try {
            Student updated = administrativeService.modifyStudent(studentId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<?> removeStudent(@PathVariable Integer studentId) {
        try {
            administrativeService.removeStudent(studentId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ── Instructor user endpoints ──────────────────────────────────────────────

    @GetMapping("/instructors")
    public ResponseEntity<List<Instructor>> listInstructors() {
        return ResponseEntity.ok(administrativeService.listInstructors());
    }

    @PostMapping("/instructors")
    public ResponseEntity<?> createInstructor(@RequestBody InstructorUserRequest request) {
        try {
            Instructor created = administrativeService.createInstructor(request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/instructors/{staffId}")
    public ResponseEntity<?> modifyInstructor(@PathVariable Integer staffId, @RequestBody InstructorUserRequest request) {
        try {
            Instructor updated = administrativeService.modifyInstructor(staffId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/instructors/{staffId}")
    public ResponseEntity<?> removeInstructor(@PathVariable Integer staffId) {
        try {
            administrativeService.removeInstructor(staffId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
