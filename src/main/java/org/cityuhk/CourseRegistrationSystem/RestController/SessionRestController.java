package org.cityuhk.CourseRegistrationSystem.RestController;

import java.util.HashMap;
import java.util.Map;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
public class SessionRestController {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    public SessionRestController(AdminRepository adminRepository,
                                 StudentRepository studentRepository,
                                 InstructorRepository instructorRepository) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String userEid = authentication.getName();
        Map<String, Object> body = new HashMap<>();
        body.put("userEID", userEid);

        Admin admin = adminRepository.findByUserEID(userEid).orElse(null);
        if (admin != null) {
            body.put("role", "ADMIN");
            body.put("staffId", admin.getStaffId());
            body.put("displayName", admin.getUserName());
            return ResponseEntity.ok(body);
        }

        Student student = studentRepository.findByUserEID(userEid).orElse(null);
        if (student != null) {
            body.put("role", "STUDENT");
            body.put("studentId", student.getStudentId());
            body.put("displayName", student.getUserName());
            return ResponseEntity.ok(body);
        }

        Instructor instructor = instructorRepository.findByUserEID(userEid).orElse(null);
        if (instructor != null) {
            body.put("role", "INSTRUCTOR");
            body.put("staffId", instructor.getStaffId());
            body.put("displayName", instructor.getUserName());
            return ResponseEntity.ok(body);
        }

        return ResponseEntity.status(404).body("Authenticated user not found");
    }
}
