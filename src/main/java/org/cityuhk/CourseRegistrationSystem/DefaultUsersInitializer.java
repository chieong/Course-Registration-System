package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Model.*;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Set;

@Component
public class DefaultUsersInitializer implements CommandLineRunner {

    private final StudentRepositoryPort studentRepository;
    private final AdminRepositoryPort adminRepository;
    private final InstructorRepositoryPort instructorRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepositoryPort courseRepository;
    private final SectionRepositoryPort sectionRepository;
    private final RegistrationPeriodRepositoryPort registrationPeriodRepository;

    @Autowired
    public DefaultUsersInitializer(StudentRepositoryPort studentRepository,
                                   AdminRepositoryPort adminRepository,
                                   InstructorRepositoryPort instructorRepository,
                                   CourseRepositoryPort courseRepository,
                                   SectionRepositoryPort sectionRepository,
                                   RegistrationPeriodRepositoryPort registrationPeriodRepository) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Admins
        seedAdmin("admin", "Admin", "admin123");
        seedAdmin("kobyashi", "Kobyashi", "kobyashi123");

        // 2. Seed Instructors
        seedInstructor("sakiko", "Sakiko Togawa", "saki123", "Classical Composition");
        seedInstructor("umiri", "Umiri Yahata", "umiri123", "Computer Science");

        Course cs101 = seedCourse("CS101", "Intro to Java", 3, "Basics of Programming", Set.of(), Set.of(),null);

        // Course B: Requires CS101
        Course cs202 = seedCourse("CS202", "Data Structures", 3, "Advanced Algorithms", Set.of(cs101), Set.of(),null);

        // Course C: Mutually Exclusive with CS202
        Course ge103 = seedCourse("GE103", "Digital Literacy", 3, "Computing for non-majors", Set.of(), Set.of(cs202),null);

        // 4. Seed Sections (3 for each course)
        // Sections for CS101
        seedSection(cs101, 30, 10, LocalTime.of(9, 0), LocalTime.of(12, 0), 'M', "Room 101");
        seedSection(cs101, 30, 10, LocalTime.of(14, 0), LocalTime.of(17, 0), 'W', "Room 101");
        seedSection(cs101, 2, 1, LocalTime.of(9, 0), LocalTime.of(12, 0), 'F', "Lab A"); // Tiny capacity for waitlist test

        // Sections for CS202
        seedSection(cs202, 25, 5, LocalTime.of(10, 0), LocalTime.of(13, 0), 'M', "Room 202"); // Conflict with CS101 Sec 1
        seedSection(cs202, 25, 5, LocalTime.of(9, 0), LocalTime.of(12, 0), 'T', "Room 202");
        seedSection(cs202, 25, 5, LocalTime.of(13, 0), LocalTime.of(16, 0), 'R', "Room 202");

        // Sections for GE103
        seedSection(ge103, 40, 20, LocalTime.of(14, 0), LocalTime.of(17, 0), 'W', "Online");
        seedSection(ge103, 40, 20, LocalTime.of(14, 0), LocalTime.of(17, 0), 'F', "Online");
        seedSection(ge103, 40, 20, LocalTime.of(10, 0), LocalTime.of(13, 0), 'S', "LT-5");

        // 5. Seed Students with Completed Course Data
        // Tomori has finished CS101 (Can take CS202)
        seedStudent("tomori01", "Tomori Takamatsu", "tomori123", "Literature", 2024, "Humanities", 12, 18, 120, Set.of(cs101));

        // Anon has no completed courses (Blocked from CS202)
        seedStudent("anon01", "Anon Chihaya", "anon123", "International Studies", 2024, "Social Sciences", 15, 21, 128, Set.of());

        seedStudent("rana01", "Raana Kaname", "rana123", "Music Performance", 2024, "Arts", 9, 15, 120, Set.of());
        seedStudent("soyo01", "Soyo Nagasaki", "soyo123", "Political Science", 2024, "Social Sciences", 12, 18, 130, Set.of());
        seedStudent("taki01", "Taki Shiina", "taki123", "Music Production", 2024, "Arts", 12, 18, 120, Set.of());

        // 6. Seed Registration Period
        seedRegistrationPeriod(2024, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
    }

    private void seedStudent(String eid, String name, String rawPassword, String major, int cohort, String department, int minSemesterCredit, int maxSemesterCredit, int maxDegreeCredit, Set<Course> completedCourses) {
        if (studentRepository.findByUserEID(eid).isPresent()) return;

        Student u = new Student.StudentBuilder()
                .withUserEID(eid)
                .withName(name)
                .withPassword(passwordEncoder.encode(rawPassword))
                .withMajor(major)
                .withCohort(cohort)
                .withDepartment(department)
                .withMinSemesterCredit(minSemesterCredit)
                .withMaxSemesterCredit(maxSemesterCredit)
                .withMaxDegreeCredit(maxDegreeCredit)
                .withCompletedCourses(completedCourses)
                .build();

        studentRepository.save(u);
    }

    private void seedInstructor(String eid, String name, String rawPassword, String department) {
        if (instructorRepository.findByUserEID(eid).isPresent()) return;

        Instructor i = new Instructor.InstructorBuilder()
                .withUserEID(eid)
                .withName(name)
                .withPassword(passwordEncoder.encode(rawPassword))
                .withDepartment(department)
                .build();

        instructorRepository.save(i);
    }

    private void seedAdmin(String eid, String name, String rawPassword) {
        if (adminRepository.findByUserEID(eid).isPresent()) return;

        Admin a = new Admin.AdminBuilder()
                .withUserEID(eid)
                .withName(name)
                .withPassword(passwordEncoder.encode(rawPassword))
                .build();

        adminRepository.save(a);
    }

    private Course seedCourse(String courseCode, String title, int credits, String description, Set<Course> prerequisites, Set<Course> exclusive, Set<Section> sections) {
        return courseRepository.findByCourseCode(courseCode)
                .orElseGet(() -> {
                    Course course = new Course(courseCode, title, credits, description, prerequisites, exclusive, sections);
                    return courseRepository.save(course);
                });
    }

    private void seedSection(Course course, int enrollCap, int waitCap, LocalTime startTime, LocalTime endTime, char weekdayChar, String venue) {
        DayOfWeek dayOfWeek = mapCharToDayOfWeek(weekdayChar);

        // Align with the logic in your request parser:
        // Use LocalDate.EPOCH as the base, then find the 'next' occurrence of that day.
        LocalDate baseDate = LocalDate.EPOCH.with(TemporalAdjusters.next(dayOfWeek));

        LocalDateTime start = LocalDateTime.of(baseDate, startTime);
        LocalDateTime end = LocalDateTime.of(baseDate, endTime);

        Section section = new Section(course, enrollCap, waitCap, start, end, venue);
        sectionRepository.save(section);
    }

    private DayOfWeek mapCharToDayOfWeek(char c) {
        return switch (Character.toUpperCase(c)) {
            case 'M' -> DayOfWeek.MONDAY;
            case 'T' -> DayOfWeek.TUESDAY;
            case 'W' -> DayOfWeek.WEDNESDAY;
            case 'R' -> DayOfWeek.THURSDAY;
            case 'F' -> DayOfWeek.FRIDAY;
            case 'S' -> DayOfWeek.SATURDAY;
            case 'U' -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Invalid weekday: " + c);
        };
    }

    private void seedRegistrationPeriod(int cohort, LocalDateTime startTime, LocalDateTime endTime) {
        if (!registrationPeriodRepository.findAll().isEmpty()) return;
        RegistrationPeriod p = new RegistrationPeriod(cohort, startTime, endTime);
        registrationPeriodRepository.save(p);
    }
}