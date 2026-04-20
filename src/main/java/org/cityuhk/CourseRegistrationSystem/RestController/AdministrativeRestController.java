package org.cityuhk.CourseRegistrationSystem.RestController;

import java.util.List;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
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
    public ResponseEntity<Admin> createUser(@RequestBody AdminUserRequest request) {
        Admin created = administrativeService.createUser(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/users/{staffId}")
    public ResponseEntity<Admin> modifyUser(@PathVariable Integer staffId, @RequestBody AdminUserRequest request) {
        Admin updated = administrativeService.modifyUser(staffId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{staffId}")
    public ResponseEntity<Void> removeUser(@PathVariable Integer staffId) {
        administrativeService.removeUser(staffId);
        return ResponseEntity.noContent().build();
    }

    // ── Student user endpoints ─────────────────────────────────────────────────

    @GetMapping("/students")
    public ResponseEntity<List<Student>> listStudents() {
        return ResponseEntity.ok(administrativeService.listStudents());
    }

    @PostMapping("/students")
    public ResponseEntity<Student> createStudent(@RequestBody StudentUserRequest request) {
        Student created = administrativeService.createStudent(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/students/{studentId}")
    public ResponseEntity<Student> modifyStudent(@PathVariable Integer studentId, @RequestBody StudentUserRequest request) {
        Student updated = administrativeService.modifyStudent(studentId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<Void> removeStudent(@PathVariable Integer studentId) {
        administrativeService.removeStudent(studentId);
        return ResponseEntity.noContent().build();
    }

    // ── Instructor user endpoints ──────────────────────────────────────────────

    @GetMapping("/instructors")
    public ResponseEntity<List<Instructor>> listInstructors() {
        return ResponseEntity.ok(administrativeService.listInstructors());
    }

    @PostMapping("/instructors")
    public ResponseEntity<Instructor> createInstructor(@RequestBody InstructorUserRequest request) {
        Instructor created = administrativeService.createInstructor(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/instructors/{staffId}")
    public ResponseEntity<Instructor> modifyInstructor(@PathVariable Integer staffId, @RequestBody InstructorUserRequest request) {
        Instructor updated = administrativeService.modifyInstructor(staffId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/instructors/{staffId}")
    public ResponseEntity<Void> removeInstructor(@PathVariable Integer staffId) {
        administrativeService.removeInstructor(staffId);
        return ResponseEntity.noContent().build();
    }
}
