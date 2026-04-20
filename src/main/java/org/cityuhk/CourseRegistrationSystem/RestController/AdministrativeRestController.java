package org.cityuhk.CourseRegistrationSystem.RestController;

import java.util.List;
import java.util.stream.Collectors;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminInstructorRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminStudentRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.InstructorUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.StudentUserRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodOverlapException;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodValidationException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdministrativeRestController {

    public record StudentAdminResponse(
            Integer studentId,
            String userEID,
            String name,
            String major,
            String department,
            Integer cohort,
            Integer minSemesterCredit,
            Integer maxSemesterCredit,
            Integer maxDegreeCredit) {
        static StudentAdminResponse from(Student student) {
            return new StudentAdminResponse(
                    student.getStudentId(),
                    student.getUserEID(),
                    student.getUserName(),
                    student.getMajor(),
                    student.getDepartment(),
                    student.getCohort(),
                    student.getMinSemesterCredit(),
                    student.getMaxSemesterCredit(),
                    student.getMaxDegreeCredit());
        }
    }

    public record InstructorAdminResponse(
            Integer staffId,
            String userEID,
            String name,
            String department) {
        static InstructorAdminResponse from(Instructor instructor) {
            return new InstructorAdminResponse(
                    instructor.getStaffId(),
                    instructor.getUserEID(),
                    instructor.getUserName(),
                    instructor.getDepartment());
        }
    }

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

    @GetMapping("/students")
    public ResponseEntity<List<StudentAdminResponse>> listStudents() {
        List<StudentAdminResponse> response =
                administrativeService.listStudents().stream()
                        .map(StudentAdminResponse::from)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@RequestBody AdminStudentRequest request) {
        try {
            Student created = administrativeService.createStudent(request);
            return ResponseEntity.ok(StudentAdminResponse.from(created));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/students/{studentId}")
    public ResponseEntity<?> modifyStudent(
            @PathVariable Integer studentId,
            @RequestBody AdminStudentRequest request) {
        try {
            Student updated = administrativeService.modifyStudent(studentId, request);
            return ResponseEntity.ok(StudentAdminResponse.from(updated));
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

    @GetMapping("/instructors")
    public ResponseEntity<List<InstructorAdminResponse>> listInstructors() {
        List<InstructorAdminResponse> response =
                administrativeService.listInstructors().stream()
                        .map(InstructorAdminResponse::from)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/instructors")
    public ResponseEntity<?> createInstructor(@RequestBody AdminInstructorRequest request) {
        try {
            Instructor created = administrativeService.createInstructor(request);
            return ResponseEntity.ok(InstructorAdminResponse.from(created));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/instructors/{staffId}")
    public ResponseEntity<?> modifyInstructor(
            @PathVariable Integer staffId,
            @RequestBody AdminInstructorRequest request) {
        try {
            Instructor updated = administrativeService.modifyInstructor(staffId, request);
            return ResponseEntity.ok(InstructorAdminResponse.from(updated));
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

    @GetMapping("/periods")
    public ResponseEntity<List<RegistrationPeriod>> listPeriods(
            @RequestParam(required = false) Integer cohort) {
        return ResponseEntity.ok(administrativeService.listRegistrationPeriods(cohort));
    }

    @PostMapping("/periods")
    public ResponseEntity<?> createPeriod(@RequestBody AdminPeriodRequest request) {
        try {
            administrativeService.createRegistrationPeriod(request);
            List<RegistrationPeriod> updated = administrativeService.listRegistrationPeriods(null);
            return ResponseEntity.ok(updated);
        } catch (RegistrationPeriodOverlapException | RegistrationPeriodValidationException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/periods/{periodId}")
    public ResponseEntity<?> deletePeriod(@PathVariable Integer periodId) {
        try {
            administrativeService.deleteRegistrationPeriod(periodId);
            List<RegistrationPeriod> updated = administrativeService.listRegistrationPeriods(null);
            return ResponseEntity.ok(updated);
        } catch (RegistrationPeriodValidationException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
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
