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

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.InstructorUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.StudentUserRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationPlanService;
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
    private final RegistrationPlanService registrationPlanService;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;
    private final CliSessionStore sessionStore;

    private CliSession activeSession;
    private boolean running;

    public InteractiveCliRunner(
            CourseService courseService,
            RegistrationService registrationService,
            TimetableService timetableService,
            AdministrativeService administrativeService,
            RegistrationPlanService registrationPlanService,
            AdminRepository adminRepository,
            StudentRepository studentRepository,
            InstructorRepository instructorRepository,
            PasswordEncoder passwordEncoder,
            CliSessionStore sessionStore) {
        this.courseService = courseService;
        this.registrationService = registrationService;
        this.timetableService = timetableService;
        this.administrativeService = administrativeService;
        this.registrationPlanService = registrationPlanService;
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionStore = sessionStore;
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
            case "list-plans":
                handleListPlans();
                return;
            case "create-plan":
                handleCreatePlan(args);
                return;
            case "remove-plan":
                handleRemovePlan(args);
                return;
            case "add-plan-entry":
                handleAddPlanEntry(args);
                return;
            case "remove-plan-entry":
                handleRemovePlanEntry(args);
                return;
            case "reorder-plans":
                handleReorderPlans(args);
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
            case "admin-list-students":
                handleAdminListStudents();
                return;
            case "admin-create-student":
                handleAdminCreateStudent(args);
                return;
            case "admin-modify-student":
                handleAdminModifyStudent(args);
                return;
            case "admin-remove-student":
                handleAdminRemoveStudent(args);
                return;
            case "admin-list-instructors":
                handleAdminListInstructors();
                return;
            case "admin-create-instructor":
                handleAdminCreateInstructor(args);
                return;
            case "admin-modify-instructor":
                handleAdminModifyInstructor(args);
                return;
            case "admin-remove-instructor":
                handleAdminRemoveInstructor(args);
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
        System.out.println("  list-plans");
        System.out.println("  create-plan [priority]");
        System.out.println("  remove-plan <planId>");
        System.out.println("  add-plan-entry <planId> <sectionId> <SELECTED|WAITLIST> [joinWaitlistOnAddFailure]");
        System.out.println("  remove-plan-entry <planId> <entryId>");
        System.out.println("  reorder-plans <planIdCsv>");
        System.out.println("  admin-list-users");
        System.out.println("  admin-create-user <userEID> <name> <password>");
        System.out.println("  admin-modify-user <staffId> <userEID> <name> [password]");
        System.out.println("  admin-remove-user <staffId>");
        System.out.println("  admin-list-students");
        System.out.println("  admin-create-student <userEID> <name> <password> [--major <major>] [--dept <dept>]");
        System.out.println("  admin-modify-student <studentId> <userEID> <name> [password] [--major <major>] [--dept <dept>]");
        System.out.println("  admin-remove-student <studentId>");
        System.out.println("  admin-list-instructors");
        System.out.println("  admin-create-instructor <userEID> <name> <password> [--dept <dept>]");
        System.out.println("  admin-modify-instructor <staffId> <userEID> <name> [password] [--dept <dept>]");
        System.out.println("  admin-remove-instructor <staffId>");
        System.out.println("  admin-create-course --code <code> --title <title> --credits <credits> [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]");
        System.out.println("  admin-modify-course --code <code> [--title <title>] [--credits <credits>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]");
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
        } else if (session.getRole() == CliRole.INSTRUCTOR) {
            valid = instructorRepository.findByUserEID(session.getUserEid()).isPresent();
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

        Optional<Instructor> instructor = instructorRepository.findByUserEID(userEid)
                .filter(i -> passwordMatches(password, i.getPassword()));
        if (instructor.isPresent()) {
            activeSession = new CliSession(instructor.get().getUserEID(), CliRole.INSTRUCTOR);
            sessionStore.save(activeSession);
            System.out.println("Logged in as INSTRUCTOR: " + instructor.get().getUserEID());
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
        Student student = requireStudent();
        if (args.size() > 1) {
            throw new IllegalArgumentException("Usage: export-timetable [outputPath]");
        }

        Path generated = timetableService.exportTimetable(student.getStudentId());
        Path outputPath;
        if (args.isEmpty()) {
            outputPath = Paths.get("student-" + student.getStudentId() + "-timetable.txt").toAbsolutePath();
        } else {
            outputPath = Paths.get(args.get(0)).toAbsolutePath();
        }

        Files.copy(generated, outputPath, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(generated);
        System.out.println("Timetable exported to " + outputPath);
    }

    private void handleListPlans() {
        Student student = requireStudent();
        List<RegistrationPlan> plans = registrationPlanService.getPlanSet(student.getStudentId());
        if (plans.isEmpty()) {
            System.out.println("No plans found.");
            return;
        }

        List<RegistrationPlan> sortedPlans = plans.stream()
                .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .collect(Collectors.toList());

        for (RegistrationPlan plan : sortedPlans) {
            int entryCount = plan.getEntries() == null ? 0 : plan.getEntries().size();
            System.out.println(
                    "planId=" + plan.getPlanId()
                            + " | priority=" + plan.getPriority()
                            + " | status=" + plan.getApplyStatus()
                            + " | entries=" + entryCount
                            + " | summary=" + valueOrDash(plan.getApplySummary()));
            if (plan.getEntries() == null) {
                continue;
            }
            for (PlanEntry entry : plan.getEntries()) {
                Integer sectionId = entry.getSection() == null ? null : entry.getSection().getSectionId();
                String sectionText = sectionId == null ? "-" : sectionId.toString();
                System.out.println(
                        "  entryId=" + entry.getEntryId()
                                + " | sectionId=" + sectionText
                                + " | type=" + entry.getEntryType()
                                + " | status=" + entry.getStatus()
                                + " | joinWaitlistOnAddFailure=" + entry.isJoinWaitlistOnAddFailure()
                                + " | reason=" + valueOrDash(entry.getFailureReason()));
            }
        }
    }

    private void handleCreatePlan(List<String> args) {
        Student student = requireStudent();
        if (args.size() > 1) {
            throw new IllegalArgumentException("Usage: create-plan [priority]");
        }

        Integer priority = args.isEmpty() ? null : parseInteger(args.get(0), "priority");
        RegistrationPlan plan = registrationPlanService.createPlan(student.getStudentId(), priority);
        System.out.println("Created plan " + plan.getPlanId() + " with priority=" + plan.getPriority());
    }

    private void handleRemovePlan(List<String> args) {
        requireStudent();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: remove-plan <planId>");
        }

        int planId = parseInteger(args.get(0), "planId");
        registrationPlanService.removePlan(planId);
        System.out.println("Removed plan " + planId);
    }

    private void handleAddPlanEntry(List<String> args) {
        requireStudent();
        if (args.size() != 3 && args.size() != 4) {
            throw new IllegalArgumentException("Usage: add-plan-entry <planId> <sectionId> <SELECTED|WAITLIST> [joinWaitlistOnAddFailure]");
        }

        int planId = parseInteger(args.get(0), "planId");
        int sectionId = parseInteger(args.get(1), "sectionId");

        PlanEntry.EntryType entryType;
        try {
            entryType = PlanEntry.EntryType.valueOf(args.get(2).trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid entry type. Use SELECTED or WAITLIST");
        }

        boolean joinWaitlistOnAddFailure = args.size() == 4 && Boolean.parseBoolean(args.get(3));
        PlanEntry created = registrationPlanService.addEntry(planId, sectionId, entryType, joinWaitlistOnAddFailure);
        System.out.println("Added plan entry " + created.getEntryId());
    }

    private void handleRemovePlanEntry(List<String> args) {
        requireStudent();
        if (args.size() != 2) {
            throw new IllegalArgumentException("Usage: remove-plan-entry <planId> <entryId>");
        }

        int planId = parseInteger(args.get(0), "planId");
        int entryId = parseInteger(args.get(1), "entryId");
        registrationPlanService.removeEntry(planId, entryId);
        System.out.println("Removed plan entry " + entryId);
    }

    private void handleReorderPlans(List<String> args) {
        Student student = requireStudent();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: reorder-plans <planIdCsv>");
        }

        String csv = args.get(0);
        List<Integer> planIds = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(value -> parseInteger(value, "planId"))
                .collect(Collectors.toList());

        if (planIds.isEmpty()) {
            throw new IllegalArgumentException("planIdCsv must contain at least one planId");
        }

        List<RegistrationPlan> reordered = registrationPlanService.reorderPlans(student.getStudentId(), planIds);
        System.out.println("Plans reordered.");
        for (RegistrationPlan plan : reordered) {
            System.out.println("planId=" + plan.getPlanId() + " | priority=" + plan.getPriority());
        }
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

    private void handleAdminListStudents() {
        requireAdminSession();
        List<Student> students = administrativeService.listStudents();
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        for (Student s : students) {
            System.out.println(s.getStudentId() + " | " + s.getUserEID() + " | " + s.getUserName()
                    + " | major=" + valueOrDash(s.getMajor())
                    + " | dept=" + valueOrDash(s.getDepartment()));
        }
    }

    private void handleAdminCreateStudent(List<String> args) {
        requireAdminSession();
        if (args.size() < 3) {
            throw new IllegalArgumentException(
                    "Usage: admin-create-student <userEID> <name> <password> [--major <major>] [--dept <dept>]");
        }

        List<String> positional = args.subList(0, 3);
        Map<String, String> options = CliCommandParser.parseOptions(args.subList(3, args.size()));

        StudentUserRequest request = new StudentUserRequest();
        request.setUserEID(positional.get(0));
        request.setName(positional.get(1));
        request.setPassword(positional.get(2));
        request.setMajor(options.get("major"));
        request.setDepartment(options.get("dept"));

        Student created = administrativeService.createStudent(request);
        System.out.println("Created student with studentId=" + created.getStudentId());
    }

    private void handleAdminModifyStudent(List<String> args) {
        requireAdminSession();
        if (args.size() < 3) {
            throw new IllegalArgumentException(
                    "Usage: admin-modify-student <studentId> <userEID> <name> [password] [--major <major>] [--dept <dept>]");
        }

        int studentId = parseInteger(args.get(0), "studentId");

        // Separate positional from option args (options start with --)
        int optionStart = 1;
        while (optionStart < args.size() && !args.get(optionStart).startsWith("--")) {
            optionStart++;
        }
        List<String> positional = args.subList(1, optionStart);
        Map<String, String> options = CliCommandParser.parseOptions(args.subList(optionStart, args.size()));

        if (positional.size() < 2) {
            throw new IllegalArgumentException(
                    "Usage: admin-modify-student <studentId> <userEID> <name> [password] [--major <major>] [--dept <dept>]");
        }

        StudentUserRequest request = new StudentUserRequest();
        request.setUserEID(positional.get(0));
        request.setName(positional.get(1));
        if (positional.size() >= 3) {
            request.setPassword(positional.get(2));
        }
        request.setMajor(options.get("major"));
        request.setDepartment(options.get("dept"));

        Student updated = administrativeService.modifyStudent(studentId, request);
        System.out.println("Updated student " + updated.getStudentId());
    }

    private void handleAdminRemoveStudent(List<String> args) {
        requireAdminSession();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: admin-remove-student <studentId>");
        }
        int studentId = parseInteger(args.get(0), "studentId");
        administrativeService.removeStudent(studentId);
        System.out.println("Removed student " + studentId);
    }

    private void handleAdminListInstructors() {
        requireAdminSession();
        List<Instructor> instructors = administrativeService.listInstructors();
        if (instructors.isEmpty()) {
            System.out.println("No instructors found.");
            return;
        }
        for (Instructor i : instructors) {
            System.out.println(i.getStaffId() + " | " + i.getUserEID() + " | " + i.getUserName()
                    + " | dept=" + valueOrDash(i.getDepartment()));
        }
    }

    private void handleAdminCreateInstructor(List<String> args) {
        requireAdminSession();
        if (args.size() < 3) {
            throw new IllegalArgumentException(
                    "Usage: admin-create-instructor <userEID> <name> <password> [--dept <dept>]");
        }

        List<String> positional = args.subList(0, 3);
        Map<String, String> options = CliCommandParser.parseOptions(args.subList(3, args.size()));

        InstructorUserRequest request = new InstructorUserRequest();
        request.setUserEID(positional.get(0));
        request.setName(positional.get(1));
        request.setPassword(positional.get(2));
        request.setDepartment(options.get("dept"));

        Instructor created = administrativeService.createInstructor(request);
        System.out.println("Created instructor with staffId=" + created.getStaffId());
    }

    private void handleAdminModifyInstructor(List<String> args) {
        requireAdminSession();
        if (args.size() < 3) {
            throw new IllegalArgumentException(
                    "Usage: admin-modify-instructor <staffId> <userEID> <name> [password] [--dept <dept>]");
        }

        int staffId = parseInteger(args.get(0), "staffId");

        int optionStart = 1;
        while (optionStart < args.size() && !args.get(optionStart).startsWith("--")) {
            optionStart++;
        }
        List<String> positional = args.subList(1, optionStart);
        Map<String, String> options = CliCommandParser.parseOptions(args.subList(optionStart, args.size()));

        if (positional.size() < 2) {
            throw new IllegalArgumentException(
                    "Usage: admin-modify-instructor <staffId> <userEID> <name> [password] [--dept <dept>]");
        }

        InstructorUserRequest request = new InstructorUserRequest();
        request.setUserEID(positional.get(0));
        request.setName(positional.get(1));
        if (positional.size() >= 3) {
            request.setPassword(positional.get(2));
        }
        request.setDepartment(options.get("dept"));

        Instructor updated = administrativeService.modifyInstructor(staffId, request);
        System.out.println("Updated instructor " + updated.getStaffId());
    }

    private void handleAdminRemoveInstructor(List<String> args) {
        requireAdminSession();
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: admin-remove-instructor <staffId>");
        }
        int staffId = parseInteger(args.get(0), "staffId");
        administrativeService.removeInstructor(staffId);
        System.out.println("Removed instructor " + staffId);
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
        validateCourseOptions(options, isCreate);
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

    private void validateCourseOptions(Map<String, String> options, boolean isCreate) {
        Set<String> allowed = Set.of("code", "title", "credits", "description", "prereq", "exclusive");
        List<String> unknown = options.keySet().stream()
                .filter(key -> !allowed.contains(key))
                .sorted()
                .collect(Collectors.toList());

        if (!unknown.isEmpty()) {
            String command = isCreate ? "admin-create-course" : "admin-modify-course";
            String unknownOptions = unknown.stream()
                    .map(key -> "--" + key)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Unknown option(s) for " + command + ": " + unknownOptions);
        }
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
