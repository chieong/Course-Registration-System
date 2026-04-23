package org.cityuhk.CourseRegistrationSystem.Cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.cityuhk.CourseRegistrationSystem.Model.*;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true")
public class InteractiveCliRunner implements CommandLineRunner {

    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final TimetableService timetableService;
    private final AdministrativeService administrativeService;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CliSessionStore sessionStore;
    private final InstructorRepository instructorRepository;

    private CliSession activeSession;
    private boolean running;

    public InteractiveCliRunner(
            CourseService courseService,
            RegistrationService registrationService,
            TimetableService timetableService,
            AdministrativeService administrativeService,
            AdminRepository adminRepository,
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder,
            CliSessionStore sessionStore,
            InstructorRepository instructorRepository) {
        this.courseService = courseService;
        this.registrationService = registrationService;
        this.timetableService = timetableService;
        this.administrativeService = administrativeService;
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionStore = sessionStore;
        this.instructorRepository = instructorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadSavedSession();
        printWelcome();

        running = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (running) {
            System.out.print("crs> ");
            String line = reader.readLine();
            if (line == null) {
                running = false;
                continue;
            }

            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                handleLine(line);
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }

        System.out.println("CLI session closed.");
    }

    private void handleLine(String line) throws Exception {
        List<String> tokens = CliCommandParser.tokenize(line);
        if (tokens.isEmpty()) {
            return;
        }

        String command = tokens.get(0).toLowerCase();
        List<String> args = tokens.subList(1, tokens.size());

        switch (command) {
            case "help":
                printHelp();
                return;
            case "exit":
            case "quit":
                running = false;
                return;
            case "login":
                handleLogin(args);
                return;
            case "logout":
                handleLogout();
                return;
            case "whoami":
                handleWhoAmI();
                return;
            case "list-courses":
                handleListCourses();
                return;
            case "add-section":
                handleAddSection(args);
                return;
            case "drop-section":
                handleDropSection(args);
                return;
            case "join-waitlist":
                handleJoinWaitlist(args);
                return;
            case "export-timetable":
                handleExportTimetable(args);
                return;
            case "admin-list-users":
                handleAdminListUsers();
                return;
            case "admin-create-user":
                handleAdminCreateUser(args);
                return;
            case "admin-modify-user":
                handleAdminModifyUser(args);
                return;
            case "admin-remove-user":
                handleAdminRemoveUser(args);
                return;
            case "admin-create-course":
                handleAdminCreateCourse(args);
                return;
            case "admin-modify-course":
                handleAdminModifyCourse(args);
                return;
            case "admin-remove-course":
                handleAdminRemoveCourse(args);
                return;
            case "admin-list-periods":
                handleAdminListPeriods(args);
                return;
            case "admin-create-period":
                handleAdminCreatePeriod(args);
                return;
            case "admin-delete-period":
                handleAdminDeletePeriod(args);
                return;
            default:
                System.out.println("Unknown command. Type help to see available commands.");
        }
    }

    private void printWelcome() {
        System.out.println("Course Registration CLI");
        System.out.println("Type help to list commands.");
        if (activeSession != null) {
            System.out.println("Restored session for " + activeSession.getUserEid() + " (" + activeSession.getRole() + ").");
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help");
        System.out.println("  exit | quit");
        System.out.println("  login <userEID> <password>");
        System.out.println("  logout");
        System.out.println("  whoami");
        System.out.println("  list-courses");
        System.out.println("  add-section <sectionId>");
        System.out.println("  drop-section <sectionId>");
        System.out.println("  join-waitlist <sectionId>");
        System.out.println("  export-timetable [outputPath]");
        System.out.println("  admin-list-users");
        System.out.println("  admin-create-user <userEID> <name> <password>");
        System.out.println("  admin-modify-user <staffId> <userEID> <name> [password]");
        System.out.println("  admin-remove-user <staffId>");
        System.out.println("  admin-create-course --code <code> --title <title> --credits <credits> [--term <term>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]");
        System.out.println("  admin-modify-course --code <code> [--title <title>] [--credits <credits>] [--term <term>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]");
        System.out.println("  admin-remove-course <courseCode>");
        System.out.println("  admin-list-periods [--cohort <cohort>]");
        System.out.println("  admin-create-period --cohort <cohort> --term <term> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm>");
        System.out.println("  admin-delete-period <periodId>");
        System.out.println("Use double quotes for values with spaces.");
    }

    private void loadSavedSession() {
        Optional<CliSession> loaded = sessionStore.load();
        if (loaded.isEmpty()) {
            return;
        }

        CliSession session = loaded.get();
        boolean valid;
        if (session.getRole() == CliRole.ADMIN) {
            valid = adminRepository.findByUserEID(session.getUserEid()).isPresent();
        } else {
            valid = studentRepository.findByUserEID(session.getUserEid()).isPresent();
        }

        if (valid) {
            activeSession = session;
        } else {
            try {
                sessionStore.clear();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleLogin(List<String> args) throws IOException {
        if (args.size() != 2) {
            throw new IllegalArgumentException("Usage: login <userEID> <password>");
        }

        String userEid = args.get(0);
        String password = args.get(1);

        Optional<Admin> admin = adminRepository.findByUserEID(userEid)
                .filter(a -> passwordMatches(password, a.getPassword()));
        if (admin.isPresent()) {
            activeSession = new CliSession(admin.get().getUserEID(), CliRole.ADMIN);
            sessionStore.save(activeSession);
            System.out.println("Logged in as ADMIN: " + admin.get().getUserEID());
            return;
        }

        Optional<Student> student = studentRepository.findByUserEID(userEid)
                .filter(s -> passwordMatches(password, s.getPassword()));
        if (student.isPresent()) {
            activeSession = new CliSession(student.get().getUserEID(), CliRole.STUDENT);
            sessionStore.save(activeSession);
            System.out.println("Logged in as STUDENT: " + student.get().getUserEID());
            return;
        }

        throw new IllegalArgumentException("Invalid credentials");
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null || rawPassword == null) {
            return false;
        }

        try {
            if (passwordEncoder.matches(rawPassword, storedPassword)) {
                return true;
            }
        } catch (IllegalArgumentException ignored) {
        }

        return rawPassword.equals(storedPassword);
    }

    private void handleLogout() throws IOException {
        activeSession = null;
        sessionStore.clear();
        System.out.println("Logged out.");
    }

    private void handleWhoAmI() {
        if (activeSession == null) {
            System.out.println("No active session.");
            return;
        }
        System.out.println(activeSession.getUserEid() + " (" + activeSession.getRole() + ")");
    }

    private void handleListCourses() {
        List<Course> courses = courseService.getAllCourses();
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }

        for (Course course : courses) {
            int sectionCount = course.getSections() == null ? 0 : course.getSections().size();
            System.out.println(
                    course.getCourseCode() + " | "
                            + course.getTitle() + " | credits=" + course.getCredits()
                            + " | sections=" + sectionCount);
        }
    }

    private void handleAddSection(List<String> args) {
        Student student = requireStudent();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: add-section <sectionId>");
        }
        int sectionId = parseInteger(args.get(0), "sectionId");
        registrationService.addSection(student.getStudentId(), sectionId, LocalDateTime.now());
        System.out.println("Registration added.");
    }

    private void handleDropSection(List<String> args) {
        Student student = requireStudent();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: drop-section <sectionId>");
        }
        int sectionId = parseInteger(args.get(0), "sectionId");
        registrationService.dropSection(student.getStudentId(), sectionId, LocalDateTime.now());
        System.out.println("Registration dropped.");
    }

    private void handleJoinWaitlist(List<String> args) {
        Student student = requireStudent();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: join-waitlist <sectionId>");
        }
        int sectionId = parseInteger(args.get(0), "sectionId");
        registrationService.waitListSection(student.getStudentId(), sectionId, LocalDateTime.now());
        System.out.println("Added to waitlist.");
    }

    private void handleExportTimetable(List<String> args) throws Exception {

        if (args.size() > 1) {
            throw new IllegalArgumentException("Usage: export-timetable [outputPath]");
        }
        Path generated = null;
        Path outputPath = null;
        if(activeSession.getRole() ==  CliRole.STUDENT) {
            Student student = requireStudent();
            generated = timetableService.exportStudentTimetable(student.getStudentId());
            if (args.isEmpty()) {
                outputPath = Paths.get("student-" + student.getStudentId() + "-timetable.txt").toAbsolutePath();
            } else {
                outputPath = Paths.get(args.get(0)).toAbsolutePath();
            }


        } else if (activeSession.getRole() == CliRole.INSTRUCTOR) {
            Instructor instructor = requireInstructor();
            generated = timetableService.exportInstructorTimetable(instructor.getStaffId());
            if (args.isEmpty()) {
                outputPath = Paths.get("instructor-" + instructor.getStaffId() + "-timetable.txt").toAbsolutePath();
            } else {
                outputPath = Paths.get(args.get(0)).toAbsolutePath();
            }
        } else {
            throw new IllegalArgumentException("Usage: Only Instrucotor and Student can export timetable.");
        }

        Files.copy(generated, outputPath, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(generated);
        System.out.println("Timetable exported to " + outputPath);
    }

    private void handleAdminListUsers() {
        requireAdminSession();
        List<Admin> users = administrativeService.listUsers();
        if (users.isEmpty()) {
            System.out.println("No admin users found.");
            return;
        }

        for (Admin admin : users) {
            System.out.println(admin.getStaffId() + " | " + admin.getUserEID() + " | " + admin.getUserName());
        }
    }

    private void handleAdminCreateUser(List<String> args) {
        requireAdminSession();
        if (args.size() != 3) {
            throw new IllegalArgumentException("Usage: admin-create-user <userEID> <name> <password>");
        }

        AdminUserRequest request = new AdminUserRequest();
        request.setUserEID(args.get(0));
        request.setName(args.get(1));
        request.setPassword(args.get(2));

        Admin created = administrativeService.createUser(request);
        System.out.println("Created admin user with staffId=" + created.getStaffId());
    }

    private void handleAdminModifyUser(List<String> args) {
        requireAdminSession();
        if (args.size() != 3 && args.size() != 4) {
            throw new IllegalArgumentException("Usage: admin-modify-user <staffId> <userEID> <name> [password]");
        }

        int staffId = parseInteger(args.get(0), "staffId");

        AdminUserRequest request = new AdminUserRequest();
        request.setUserEID(args.get(1));
        request.setName(args.get(2));
        if (args.size() == 4) {
            request.setPassword(args.get(3));
        }

        Admin updated = administrativeService.modifyUser(staffId, request);
        System.out.println("Updated admin user " + updated.getStaffId());
    }

    private void handleAdminRemoveUser(List<String> args) {
        requireAdminSession();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: admin-remove-user <staffId>");
        }

        int staffId = parseInteger(args.get(0), "staffId");
        administrativeService.removeUser(staffId);
        System.out.println("Removed admin user " + staffId);
    }

    private void handleAdminCreateCourse(List<String> args) {
        requireAdminSession();
        AdminCourseRequest request = parseCourseRequest(args, true);
        Course created = administrativeService.createCourse(request);
        System.out.println("Created course " + created.getCourseCode());
    }

    private void handleAdminModifyCourse(List<String> args) {
        requireAdminSession();
        AdminCourseRequest request = parseCourseRequest(args, false);
        Course updated = administrativeService.modifyCourse(request);
        System.out.println("Updated course " + updated.getCourseCode());
    }

    private void handleAdminRemoveCourse(List<String> args) {
        requireAdminSession();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: admin-remove-course <courseCode>");
        }

        administrativeService.removeCourse(args.get(0));
        System.out.println("Removed course " + args.get(0));
    }

    private void handleAdminListPeriods(List<String> args) {
        requireAdminSession();
        Integer cohort = null;
        if (!args.isEmpty()) {
            Map<String, String> options = CliCommandParser.parseOptions(args);
            String cohortStr = options.get("cohort");
            if (cohortStr != null) {
                cohort = parseInteger(cohortStr, "cohort");
            }
        }
        List<RegistrationPeriod> periods = administrativeService.listRegistrationPeriods(cohort);
        printPeriodList(periods);
    }

    private void handleAdminCreatePeriod(List<String> args) {
        requireAdminSession();
        Map<String, String> options = CliCommandParser.parseOptions(args);

        String cohortStr = options.get("cohort");
        if (cohortStr == null || cohortStr.isBlank()) {
            throw new IllegalArgumentException("Usage: admin-create-period --cohort <cohort> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm>");
        }
        String startStr = options.get("start");
        if (startStr == null || startStr.isBlank()) {
            throw new IllegalArgumentException("Usage: admin-create-period --cohort <cohort> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm>");
        }
        String endStr = options.get("end");
        if (endStr == null || endStr.isBlank()) {
            throw new IllegalArgumentException("Usage: admin-create-period --cohort <cohort> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm>");
        }

        AdminPeriodRequest request = new AdminPeriodRequest();
        request.setCohort(parseInteger(cohortStr, "cohort"));
        request.setStartDate(parseDateTime(startStr, "start"));
        request.setEndDate(parseDateTime(endStr, "end"));

        administrativeService.createRegistrationPeriod(request);
        System.out.println("Registration period created.");
        List<RegistrationPeriod> periods = administrativeService.listRegistrationPeriods(null);
        printPeriodList(periods);
    }

    private void handleAdminDeletePeriod(List<String> args) {
        requireAdminSession();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: admin-delete-period <periodId>");
        }

        int periodId = parseInteger(args.get(0), "periodId");
        administrativeService.deleteRegistrationPeriod(periodId);
        System.out.println("Registration period " + periodId + " deleted.");
        List<RegistrationPeriod> periods = administrativeService.listRegistrationPeriods(null);
        printPeriodList(periods);
    }

    private void printPeriodList(List<RegistrationPeriod> periods) {
        if (periods.isEmpty()) {
            System.out.println("No registration periods found.");
            return;
        }
        for (RegistrationPeriod p : periods) {
            System.out.println(
                    p.getPeriodId() + " | cohort=" + p.getCohort()
                            + " | " + p.getStartDateTime() + " -> " + p.getEndDateTime());
        }
    }

    private AdminCourseRequest parseCourseRequest(List<String> args, boolean isCreate) {
        Map<String, String> options = CliCommandParser.parseOptions(args);
        String code = options.get("code");

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("--code is required");
        }

        AdminCourseRequest request = new AdminCourseRequest();
        request.setCourseCode(code);

        String title = options.get("title");
        String credits = options.get("credits");

        if (isCreate) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("--title is required");
            }
            if (credits == null || credits.isBlank()) {
                throw new IllegalArgumentException("--credits is required");
            }
        }

        if (title != null) {
            request.setTitle(title);
        }

        if (credits != null) {
            request.setCredits(parseInteger(credits, "credits"));
        }

        request.setDescription(options.get("description"));
        request.setPrerequisiteCourseCodes(splitCsv(options.get("prereq")));
        request.setExclusiveCourseCodes(splitCsv(options.get("exclusive")));

        return request;
    }

    private Set<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return null;
        }

        Set<String> values = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (values.isEmpty()) {
            return Collections.emptySet();
        }

        return values;
    }

    private Student requireStudent() {
        requireAuthenticated();
        if (activeSession.getRole() != CliRole.STUDENT) {
            throw new IllegalStateException("This command requires STUDENT role");
        }

        return studentRepository.findByUserEID(activeSession.getUserEid())
                .orElseThrow(() -> new IllegalStateException("Student account not found"));
    }

    private Instructor requireInstructor() {
        requireAuthenticated();
        if (activeSession.getRole() != CliRole.INSTRUCTOR) {
            throw new IllegalStateException("This command requires INSTRUCTOR role");
        }

        return instructorRepository.findByUserEID(activeSession.getUserEid()).orElseThrow(() -> new IllegalStateException("Instructor account not found"));
    }

    private void requireAdminSession() {
        requireAuthenticated();
        if (activeSession.getRole() != CliRole.ADMIN) {
            throw new IllegalStateException("This command requires ADMIN role");
        }
    }

    private void requireAuthenticated() {
        if (activeSession == null) {
            throw new IllegalStateException("Please login first");
        }
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer for " + fieldName + ": " + value);
        }
    }
    
    private LocalDateTime parseDateTime(String value, String fieldName) {
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date-time for " + fieldName + " (expected yyyy-MM-ddTHH:mm): " + value);
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
    
}
