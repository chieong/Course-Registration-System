package org.cityuhk.CourseRegistrationSystem.Service.Administrative;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.CourseRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministrativeService {

    private final AdminRepository adminRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministrativeService(
            AdminRepository adminRepository,
            CourseRepository courseRepository,
            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Admin> listUsers() {
        return adminRepository.findAll();
    }

    @Transactional
    public Admin createUser(AdminUserRequest request) {
        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new RuntimeException("User EID is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        String normalizedUserEID = request.getUserEID().trim();
        if (adminRepository.findByUserEID(normalizedUserEID).isPresent()) {
            throw new RuntimeException("User EID already exists");
        }

        Admin admin = (Admin) new Admin.AdminBuilder()
                .withUserEID(normalizedUserEID)
                .withName(request.getName().trim())
                .withPassword(passwordEncoder.encode(request.getPassword()))
                .build();

        return adminRepository.save(admin);
    }

    @Transactional
    public Admin modifyUser(Integer staffId, AdminUserRequest request) {
        Admin existingAdmin = adminRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new RuntimeException("User EID is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        String normalizedUserEID = request.getUserEID().trim();
        adminRepository.findByUserEID(normalizedUserEID).ifPresent(admin -> {
            if (admin.getStaffId() != existingAdmin.getStaffId()) {
                throw new RuntimeException("User EID already exists");
            }
        });

        String encodedPassword = existingAdmin.getPassword();
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        Admin updatedAdmin = (Admin) new Admin.AdminBuilder()
                .withStaffId(existingAdmin.getStaffId())
            .withUserEID(normalizedUserEID)
            .withName(request.getName().trim())
                .withPassword(encodedPassword)
                .build();

        return adminRepository.save(updatedAdmin);
    }

    @Transactional
    public void removeUser(Integer staffId) {
        if (!adminRepository.existsById(staffId)) {
            throw new RuntimeException("Admin user not found");
        }
        adminRepository.deleteById(staffId);
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
    public Course modifyCourse(String courseCode, AdminCourseRequest request) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new RuntimeException("Course code is required");
        }

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