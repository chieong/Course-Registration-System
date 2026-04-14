package org.cityuhk.CourseRegistrationSystem.Controller;

import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationService;
import org.cityuhk.CourseRegistrationSystem.Service.Semester;
import org.springframework.format.annotation.DateTimeFormat;
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

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addRegistration(
            @RequestParam Integer studentId,
            @RequestParam Integer sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime semesterStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime semesterEnd) {
        try {
            Semester semester = new Semester(semesterStart, semesterEnd);
            registrationService.addSection(studentId, sectionId, LocalDateTime.now(), semester);
            return ResponseEntity.ok("Registration added successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/export-timetable")
    public ResponseEntity<byte[]> exportTimeTable(@RequestParam Integer studentId) {
        try {
            Path exportedFile = registrationService.ExportTimeTable(studentId);
            byte[] fileBytes = Files.readAllBytes(exportedFile);
            Files.deleteIfExists(exportedFile);

            String filename = "student-" + studentId + "-timetable.csv";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(fileBytes);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage().getBytes());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Failed to export timetable".getBytes());
        }
    }
}