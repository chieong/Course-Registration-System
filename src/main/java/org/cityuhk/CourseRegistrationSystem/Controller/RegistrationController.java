package org.cityuhk.CourseRegistrationSystem.Controller;

import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableService;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableExportException;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
@PreAuthorize("hasRole('ADMIN')")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final TimetableService timetableService;

    public RegistrationController(RegistrationService registrationService, 
                                 TimetableService timetableService) {
        this.registrationService = registrationService;
        this.timetableService = timetableService;
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

    @GetMapping("/export-timetable")
    public ResponseEntity<byte[]> exportTimeTable(@RequestParam Integer studentId) {
        try {
            Path exportedFile = timetableService.exportTimetable(studentId);
            byte[] fileBytes = Files.readAllBytes(exportedFile);
            Files.deleteIfExists(exportedFile);

            String filename = "student-" + studentId + "-timetable.txt";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(fileBytes);
        } catch (TimetableValidationException ex) {
            // Validation errors are client errors (400 Bad Request)
            return ResponseEntity.badRequest().body(ex.getMessage().getBytes());
        } catch (TimetableExportException ex) {
            // Export errors are server errors (500 Internal Server Error)
            return ResponseEntity.internalServerError().body(("Export failed: " + ex.getMessage()).getBytes());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Failed to export timetable".getBytes());
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
