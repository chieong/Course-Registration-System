package org.cityuhk.CourseRegistrationSystem.Controller;

import java.time.LocalDateTime;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
@PreAuthorize("hasRole('ADMIN')")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addRegistration(
            @RequestParam Integer studentId,
            @RequestParam Integer sectionId) {
        try {
            registrationService.addSection(studentId, sectionId, LocalDateTime.now());
            return ResponseEntity.ok("Registration added successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/drop")
    public ResponseEntity<String> dropRegistration(
            @RequestParam Integer studentId,
            @RequestParam Integer sectionId) {
        try {
            registrationService.dropSection(studentId, sectionId, LocalDateTime.now());
            return ResponseEntity.ok("Registration dropped successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
