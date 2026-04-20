package org.cityuhk.CourseRegistrationSystem.Service.Administrative;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.InstructorUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.StudentUserRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.CourseRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.AdminUserManagementOperations;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.InstructorUserManagementOperations;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.StudentUserManagementOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministrativeService {

    private final AdminUserManagementOperations adminUserManagementService;
    private final StudentUserManagementOperations studentUserManagementService;
    private final InstructorUserManagementOperations instructorUserManagementService;
    private final CourseRepository courseRepository;

    public AdministrativeService(
            AdminUserManagementOperations adminUserManagementService,
            StudentUserManagementOperations studentUserManagementService,
            InstructorUserManagementOperations instructorUserManagementService,
            CourseRepository courseRepository) {
        this.adminUserManagementService = adminUserManagementService;
        this.studentUserManagementService = studentUserManagementService;
        this.instructorUserManagementService = instructorUserManagementService;
        this.courseRepository = courseRepository;
    }

    // ── Admin user operations ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Admin> listUsers() {
        return adminUserManagementService.listUsers();
    }

    @Transactional
    public Admin createUser(AdminUserRequest request) {
        return adminUserManagementService.createUser(request);
    }

    @Transactional
    public Admin modifyUser(Integer staffId, AdminUserRequest request) {
        return adminUserManagementService.modifyUser(staffId, request);
    }

    @Transactional
    public void removeUser(Integer staffId) {
        adminUserManagementService.removeUser(staffId);
    }

    // ── Student user operations ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Student> listStudents() {
        return studentUserManagementService.listStudents();
    }

    @Transactional
    public Student createStudent(StudentUserRequest request) {
        return studentUserManagementService.createStudent(request);
    }

    @Transactional
    public Student modifyStudent(Integer studentId, StudentUserRequest request) {
        return studentUserManagementService.modifyStudent(studentId, request);
    }

    @Transactional
    public void removeStudent(Integer studentId) {
        studentUserManagementService.removeStudent(studentId);
    }

    // ── Instructor user operations ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Instructor> listInstructors() {
        return instructorUserManagementService.listInstructors();
    }

    @Transactional
    public Instructor createInstructor(InstructorUserRequest request) {
        return instructorUserManagementService.createInstructor(request);
    }

    @Transactional
    public Instructor modifyInstructor(Integer staffId, InstructorUserRequest request) {
        return instructorUserManagementService.modifyInstructor(staffId, request);
    }

    @Transactional
    public void removeInstructor(Integer staffId) {
        instructorUserManagementService.removeInstructor(staffId);
    }

    @Transactional
    public Course createCourse(AdminCourseRequest request) {
        if (request.getCourseCode() == null || request.getCourseCode().isBlank()) {
            throw new RuntimeException("Course code is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Course title is required");
        }
        if (request.getCredits() <= 0) {
            throw new RuntimeException("Credits must be greater than 0");
        }

        String courseCode = request.getCourseCode().trim();
        if (courseRepository.existsByCourseCode(courseCode)) {
            throw new RuntimeException("Course code already exists");
        }

        Set<Course> prerequisites = resolveCourseCodes(request.getPrerequisiteCourseCodes(), "Prerequisite");
        Set<Course> exclusives = resolveCourseCodes(request.getExclusiveCourseCodes(), "Exclusive");

        if (prerequisites.stream().anyMatch(c -> c.getCourseCode().equals(courseCode))) {
            throw new RuntimeException("A course cannot be its own prerequisite");
        }
        if (exclusives.stream().anyMatch(c -> c.getCourseCode().equals(courseCode))) {
            throw new RuntimeException("A course cannot be its own exclusive course");
        }

        Set<Section> sections = request.getSections();

        Course course = new Course(
                courseCode,
                request.getTitle().trim(),
                request.getCredits(),
                request.getDescription(),
                request.getTerm(),
                prerequisites,
                exclusives,
                sections);

        return courseRepository.save(course);
    }

    @Transactional
    public Course modifyCourse(AdminCourseRequest request) {
        if (request.getCourseCode() == null || request.getCourseCode().isBlank()) {
            throw new RuntimeException("Course code is required");
        }

        String courseCode = request.getCourseCode().trim();

        Course existingCourse = courseRepository.findByCourseCode(courseCode.trim())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        String newCourseCode = request.getCourseCode() != null && !request.getCourseCode().isBlank()
                ? request.getCourseCode().trim()
                : existingCourse.getCourseCode();

        if (!newCourseCode.equals(existingCourse.getCourseCode())
                && courseRepository.existsByCourseCode(newCourseCode)) {
            throw new RuntimeException("Course code already exists");
        }
        if (request.getTitle() != null && request.getTitle().isBlank()) {
            throw new RuntimeException("Course title cannot be blank");
        }
        if (request.getCredits() < 0) {
            throw new RuntimeException("Credits cannot be negative");
        }

        if (request.getTitle() != null) {
            existingCourse.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            existingCourse.setDescription(request.getDescription());
        }
        if (request.getTerm() != null) {
            existingCourse.setTerm(request.getTerm());
        }
        if (request.getCredits() > 0) {
            existingCourse.setCredits(request.getCredits());
        }
        existingCourse.setCourseCode(newCourseCode);

        if (request.getPrerequisiteCourseCodes() != null) {
            Set<Course> prerequisites = resolveCourseCodes(request.getPrerequisiteCourseCodes(), "Prerequisite");
            if (prerequisites.stream().anyMatch(c -> c.getCourseCode().equals(newCourseCode))) {
                throw new RuntimeException("A course cannot be its own prerequisite");
            }
            existingCourse.setPrerequisiteCourses(prerequisites);
        }

        if (request.getExclusiveCourseCodes() != null) {
            Set<Course> exclusives = resolveCourseCodes(request.getExclusiveCourseCodes(), "Exclusive");
            if (exclusives.stream().anyMatch(c -> c.getCourseCode().equals(newCourseCode))) {
                throw new RuntimeException("A course cannot be its own exclusive course");
            }
            existingCourse.setExclusiveCourses(exclusives);
        }

        if (request.getSections() != null) {
            Set<Section> sections = request.getSections();
            existingCourse.setSections(sections);
        }

        return courseRepository.save(existingCourse);
    }

    @Transactional
    public void removeCourse(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new RuntimeException("Course code is required");
        }

        Course existingCourse = courseRepository.findByCourseCode(courseCode.trim())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        courseRepository.delete(existingCourse);
    }

    private Set<Course> resolveCourseCodes(Set<String> courseCodes, String relationName) {
        Set<Course> resolved = new HashSet<>();
        if (courseCodes == null) {
            return resolved;
        }

        for (String code : courseCodes) {
            if (code == null || code.isBlank()) {
                continue;
            }
            String normalizedCode = code.trim();
            Course relatedCourse = courseRepository.findByCourseCode(normalizedCode)
                    .orElseThrow(() -> new RuntimeException(relationName + " course not found: " + normalizedCode));
            resolved.add(relatedCourse);
        }

        return resolved;
    }
}