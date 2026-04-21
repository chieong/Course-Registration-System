package org.cityuhk.CourseRegistrationSystem.Service.Administrative;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.CourseRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminSectionService;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdministrativeService {
    private final AdminRepositoryPort adminRepository;
    private final CourseRepositoryPort courseRepository;
    private final SectionRepositoryPort sectionRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministrativeService(
            AdminRepositoryPort adminRepository,
            CourseRepositoryPort courseRepository,
            PasswordEncoder passwordEncoder,
            SectionRepositoryPort sectionRepository,
            RegistrationPeriodRepository registrationPeriodRepository) {
        this.adminRepository = adminRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
        this.sectionRepository = sectionRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
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

        Admin admin =
                new Admin.AdminBuilder()
                        .withUserEID(normalizedUserEID)
                        .withName(request.getName().trim())
                        .withPassword(passwordEncoder.encode(request.getPassword()))
                        .build();

        return adminRepository.save(admin);
    }

    @Transactional
    public Admin modifyUser(Integer staffId, AdminUserRequest request) {
        Admin existingAdmin =
                adminRepository
                        .findById(staffId)
                        .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new RuntimeException("User EID is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        String normalizedUserEID = request.getUserEID().trim();
        adminRepository
                .findByUserEID(normalizedUserEID)
                .ifPresent(
                        admin -> {
                            if (admin.getStaffId() != existingAdmin.getStaffId()) {
                                throw new RuntimeException("User EID already exists");
                            }
                        });

        String encodedPassword = existingAdmin.getPassword();
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        Admin updatedAdmin =
                (Admin)
                        new Admin.AdminBuilder()
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

        Set<Course> prerequisites =
                resolveCourseCodes(request.getPrerequisiteCourseCodes(), "Prerequisite");
        Set<Course> exclusives = resolveCourseCodes(request.getExclusiveCourseCodes(), "Exclusive");

        if (prerequisites.stream().anyMatch(c -> c.getCourseCode().equals(courseCode))) {
            throw new RuntimeException("A course cannot be its own prerequisite");
        }
        if (exclusives.stream().anyMatch(c -> c.getCourseCode().equals(courseCode))) {
            throw new RuntimeException("A course cannot be its own exclusive course");
        }

        Course course =
                new Course(
                        courseCode,
                        request.getTitle().trim(),
                        request.getCredits(),
                        request.getDescription(),
                        request.getTerm(),
                        prerequisites,
                        exclusives,
                        null);

        return courseRepository.save(course);
    }

    @Transactional
    public Course modifyCourse(AdminCourseRequest request) {
        if (request.getCourseCode() == null || request.getCourseCode().isBlank()) {
            throw new RuntimeException("Course code is required");
        }

        String courseCode = request.getCourseCode().trim();

        Course existingCourse =
                courseRepository
                        .findByCourseCode(courseCode.trim())
                        .orElseThrow(() -> new RuntimeException("Course not found"));

        String newCourseCode =
                request.getCourseCode() != null && !request.getCourseCode().isBlank()
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
            Set<Course> prerequisites =
                    resolveCourseCodes(request.getPrerequisiteCourseCodes(), "Prerequisite");
            if (prerequisites.stream().anyMatch(c -> c.getCourseCode().equals(newCourseCode))) {
                throw new RuntimeException("A course cannot be its own prerequisite");
            }
            existingCourse.setPrerequisiteCourses(prerequisites);
        }

        if (request.getExclusiveCourseCodes() != null) {
            Set<Course> exclusives =
                    resolveCourseCodes(request.getExclusiveCourseCodes(), "Exclusive");
            if (exclusives.stream().anyMatch(c -> c.getCourseCode().equals(newCourseCode))) {
                throw new RuntimeException("A course cannot be its own exclusive course");
            }
            existingCourse.setExclusiveCourses(exclusives);
        }

        return courseRepository.save(existingCourse);
    }

    @Transactional
    public void removeCourse(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new RuntimeException("Course code is required");
        }

        Course existingCourse =
                courseRepository
                        .findByCourseCode(courseCode.trim())
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
            Course relatedCourse =
                    courseRepository
                            .findByCourseCode(normalizedCode)
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    relationName
                                                            + " course not found: "
                                                            + normalizedCode));
            resolved.add(relatedCourse);
        }

        return resolved;
    }

    public Section createSection(AdminSectionService request) throws IllegalArgumentException {
        if (request.getCourse() == null) {
            throw new RuntimeException("Course is required");
        }
        if (request.getSectionType() == null) {
            throw new RuntimeException("SectionType is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start and end time is required");
        }
        if (request.getVenue() == null || request.getVenue().isBlank()) {
            throw new RuntimeException("Venue is required");
        }

        Section newSection =
                new Section(
                        request.getCourse(),
                        request.getEnrollCapacity(),
                        request.getWaitlistCapacity(),
                        request.getStartTime(),
                        request.getEndTime(),
                        request.getVenue());

        return sectionRepository.save(newSection);
    }

    public Section modifySection(AdminSectionService request) {
        if (request.getSectionId() == null) {
            throw new RuntimeException("SectionId is required");
        }

        Section existingSection =
                sectionRepository
                        .findById(request.getSectionId())
                        .orElseThrow(() -> new RuntimeException("Section not found"));

        if (request.getCourse() != null) {
            existingSection.setCourse(request.getCourse());
        }
        if (request.getVenue() != null && !request.getVenue().isBlank()) {
            existingSection.setVenue(request.getVenue());
        }
        if (request.getStartTime() != null) {
            existingSection.setTime(request.getStartTime(), existingSection.getEndTime());
        }
        if (request.getEndTime() != null) {
            existingSection.setTime(existingSection.getStartTime(), request.getEndTime());
        }
        if (request.getSectionType() != null) {
            existingSection.setType(request.getSectionType());
        }
        if (request.getVenue() != null) {
            existingSection.setVenue(request.getVenue());
        }
        if (request.getEnrollCapacity() != null) {
            existingSection.setEnrollCapacity(request.getEnrollCapacity());
        }
        if (request.getWaitlistCapacity() != null) {
            existingSection.setWaitlistCapacity(request.getWaitlistCapacity());
        }
        return sectionRepository.save(existingSection);
    }

    public void deleteSection(AdminSectionService request) {
        if (request.getSectionId() == null) {
            throw new RuntimeException("SectionId is required");
        }

        sectionRepository.deleteById(request.getSectionId());
    }

    public void createRegistrationPeriod(AdminPeriodRequest request) {
        if (request.getCohort() == null) {
            throw new RuntimeException("Cohort is required");
        }

        RegistrationPeriod newRegistrationPeriod =
                new RegistrationPeriod(
                        request.getCohort(), request.getStartDate(), request.getEndDate());

        registrationPeriodRepository.save(newRegistrationPeriod);
    }

    public void deleteRegistrationPeriod(AdminPeriodRequest request) {
        if (request.getPeriodId() == null) {
            throw new RuntimeException("PeriodId is required");
        }

        registrationPeriodRepository.deleteById(request.getPeriodId());
    }
}

