package org.cityuhk.CourseRegistrationSystem.Service.Administrative;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.CourseRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminInstructorRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminSectionService;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminStudentRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.AdminUserManagementOperations;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.InstructorUserManagementOperations;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.StudentUserManagementOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministrativeService {
    private final AdminRepositoryPort adminRepository;
    private final StudentRepositoryPort studentRepository;
    private final InstructorRepositoryPort instructorRepository;
    private final CourseRepositoryPort courseRepository;
    private final SectionRepositoryPort sectionRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationPeriodValidator periodValidator;
    private final AdminUserManagementOperations adminUserManagementService;
    private final StudentUserManagementOperations studentUserManagementService;
    private final InstructorUserManagementOperations instructorUserManagementService;

    public AdministrativeService(
            AdminRepositoryPort adminRepository,
            StudentRepositoryPort studentRepository,
            InstructorRepositoryPort instructorRepository,
            CourseRepositoryPort courseRepository,
            PasswordEncoder passwordEncoder,
            SectionRepositoryPort sectionRepository,
            RegistrationPeriodRepository registrationPeriodRepository,
            RegistrationPeriodValidator periodValidator,
            AdminUserManagementOperations adminUserManagementService,
            StudentUserManagementOperations studentUserManagementService,
            InstructorUserManagementOperations instructorUserManagementService) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.adminUserManagementService = adminUserManagementService;
        this.studentUserManagementService = studentUserManagementService;
        this.instructorUserManagementService = instructorUserManagementService;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
        this.sectionRepository = sectionRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
        this.periodValidator = periodValidator;
    }

    // Admin user operations

    @Transactional(readOnly = true)
    public List<Admin> listUsers() {
        return adminUserManagementService.listUsers();
    }

    @Transactional
    public Admin createUser(AdminUserRequest request) {
        return adminUserManagementService.createUser(request);
    }

    @Transactional
    public Admin modifyUser(AdminUserRequest request) {
        return adminUserManagementService.modifyUser(request);
    }

    @Transactional
    public void removeUser(String userEID) {
        adminUserManagementService.removeUser(userEID);
    }

    // Student user operations

    @Transactional(readOnly = true)
    public List<Student> listStudents() {
        return studentUserManagementService.listStudents();
    }

    @Transactional
    public Student createStudent(AdminStudentRequest request) {
        return studentUserManagementService.createStudent(request);
    }

    @Transactional
    public Student modifyStudent(AdminStudentRequest request) {
        return studentUserManagementService.modifyStudent(request);
    }

    @Transactional
    public void removeStudent(String userEID) {
        studentUserManagementService.removeStudent(userEID);
    }

    // Instructor user operations

    @Transactional(readOnly = true)
    public List<Instructor> listInstructors() {
        return instructorUserManagementService.listInstructors();
    }

    @Transactional
    public Instructor createInstructor(AdminInstructorRequest request) {
        return instructorUserManagementService.createInstructor(request);
    }

    @Transactional
    public Instructor modifyInstructor(AdminInstructorRequest request) {
        return instructorUserManagementService.modifyInstructor(request);
    }

    @Transactional
    public void removeInstructor(String userEID) {
        instructorUserManagementService.removeInstructor(userEID);
    }


    // Course operations

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

        String newCourseCode = request.getCourseCode().trim();

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
        if (request.getEnrollCapacity() == null) {
            throw new RuntimeException("Enroll capacity is required");
        }
        if (request.getWaitlistCapacity() == null) {
            throw new RuntimeException("Waitlist capacity is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start and end time is required");
        }
        if (request.getVenue() == null || request.getVenue().isBlank()) {
            throw new RuntimeException("Venue is required");
        }
        if (sectionRepository.overlapsInVenue(request.getVenue(), request.getStartTime().getDayOfWeek(), request.getStartTime().toLocalTime(), request.getStartTime().toLocalTime())) {
            throw new RuntimeException("Time conflict with existing section in same venue");
        }

        Section newSection =
                new Section(
                        request.getCourse(),
                        request.getEnrollCapacity(),
                        request.getWaitlistCapacity(),
                        request.getStartTime(),
                        request.getEndTime(),
                        request.getVenue());
        newSection.setType(request.getSectionType());
        if (request.getInstructorStaffIds() != null) {
            newSection.setInstructors(resolveInstructorIds(request.getInstructorStaffIds()));
        }

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
        if (request.getInstructorStaffIds() != null) {
            existingSection.setInstructors(resolveInstructorIds(request.getInstructorStaffIds()));
        }
        return sectionRepository.save(existingSection);
    }

    public void deleteSection(AdminSectionService request) {
        if (request.getSectionId() == null) {
            throw new RuntimeException("SectionId is required");
        }

        sectionRepository.deleteById(request.getSectionId());
    }

    @Transactional(readOnly = true)
    public List<Section> listSections(String courseCode) {
        List<Section> all = sectionRepository.findAll();
        if (courseCode == null || courseCode.isBlank()) {
            return all;
        }
        String normalized = courseCode.trim();
        List<Section> filtered =  all.stream()
                .filter(s -> s.getCourse() != null && normalized.equals(s.getCourse().getCourseCode()))
                .toList();

        //force load of courseCode and Instructor
        filtered.forEach(s -> {
            s.getInstructors().size(); // Calling .size() forces the load
            if (s.getCourse() != null) s.getCourse().getCourseCode();
        });
        return filtered;
    }

    private Set<Instructor> resolveInstructorIds(Set<Integer> instructorIds) {
        Set<Instructor> resolved = new HashSet<>();
        for (Integer id : instructorIds) {
            if (id == null) {
                continue;
            }
            Instructor instructor = instructorRepository
                    .findById(id)
                    .orElseThrow(() -> new RuntimeException("Instructor not found: " + id));
            resolved.add(instructor);
        }
        return resolved;
    }
@Transactional(readOnly = true)
    public List<RegistrationPeriod> listRegistrationPeriods(Integer cohort) {
        if (cohort != null) {
            return registrationPeriodRepository.findByCohortOrderByStartDateTime(cohort);
        }
        return registrationPeriodRepository.findAllOrderByCohortAndStartDateTime();
    }

    @Transactional
    public RegistrationPeriod createRegistrationPeriod(AdminPeriodRequest request) {
        periodValidator.validate(request);
        RegistrationPeriod newRegistrationPeriod =
                new RegistrationPeriod(
                        request.getCohort(), request.getStartDate(), request.getEndDate());
        return registrationPeriodRepository.save(newRegistrationPeriod);
    }

    @Transactional
    public void deleteRegistrationPeriod(Integer periodId) {
        if (periodId == null) {
            throw new RegistrationPeriodValidationException("Period ID is required");
        }
        if (!registrationPeriodRepository.existsById(periodId)) {
            throw new RegistrationPeriodValidationException("Registration period not found: " + periodId);
        }
        registrationPeriodRepository.deleteById(periodId);
    }

    @Transactional
    public void assignInstructor(String userEID,Integer sectionId) {
        userEID = userEID.trim();

        Instructor instructor = instructorRepository.findByUserEID(userEID).orElseThrow(()-> new RuntimeException("Instructor not found"));
        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> new RuntimeException("Section not found"));

        if(section.getInstructors().contains(instructor)) {
            throw new RegistrationPeriodValidationException("Instructor already assigned to section");
        }

        section.addInstructor(instructor);
    }

    @Transactional
    public void unassignInstructor(String userEID,Integer sectionId) {
        userEID = userEID.trim();

        Instructor instructor = instructorRepository.findByUserEID(userEID).orElseThrow(()-> new RuntimeException("Instructor not found"));
        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> new RuntimeException("Section not found"));

        if(!section.getInstructors().contains(instructor)) {
            throw new RegistrationPeriodValidationException("Instructor already unassigned to section");
        }

        section.removeInstructor(instructor);
    }
}


