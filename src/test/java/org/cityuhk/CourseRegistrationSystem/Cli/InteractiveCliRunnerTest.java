package org.cityuhk.CourseRegistrationSystem.Cli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminSectionService;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationPlanService;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InteractiveCliRunnerTest {

    @Mock
    private CourseService courseService;
    @Mock
    private RegistrationService registrationService;
    @Mock
    private TimetableService timetableService;
    @Mock
    private AdministrativeService administrativeService;
    @Mock
    private RegistrationPlanService registrationPlanService;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private InstructorRepository instructorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CliSessionStore sessionStore;

    @InjectMocks
    private InteractiveCliRunner runner;

    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream outContent;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUpStreams() {
        originalOut = System.out;
        originalIn = System.in;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    // ---------------------------
    // Helper methods
    // ---------------------------

    private void setInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String output() {
        return outContent.toString(StandardCharsets.UTF_8);
    }

    private void invokeHandleLine(String line) {
        ReflectionTestUtils.invokeMethod(runner, "handleLine", line);
    }

    private void invokeLoadSavedSession() {
        ReflectionTestUtils.invokeMethod(runner, "loadSavedSession");
    }

    private boolean invokePasswordMatches(String raw, String stored) {
        Boolean result = ReflectionTestUtils.invokeMethod(runner, "passwordMatches", raw, stored);
        return Boolean.TRUE.equals(result);
    }

    private void setAdminSession(String eid) {
        ReflectionTestUtils.setField(runner, "activeSession", new CliSession(eid, CliRole.ADMIN));
    }

    private void setStudentSession(String eid) {
        ReflectionTestUtils.setField(runner, "activeSession", new CliSession(eid, CliRole.STUDENT));
    }

    private Admin mockAdmin(String eid, String pwd, int staffId, String userName) {
        Admin admin = mock(Admin.class);
        lenient().when(admin.getUserEID()).thenReturn(eid);
        lenient().when(admin.getPassword()).thenReturn(pwd);
        lenient().when(admin.getStaffId()).thenReturn(staffId);
        lenient().when(admin.getUserName()).thenReturn(userName);
        return admin;
    }

    private Student mockStudent(String eid, String pwd, int studentId) {
        Student student = mock(Student.class);
        lenient().when(student.getUserEID()).thenReturn(eid);
        lenient().when(student.getPassword()).thenReturn(pwd);
        lenient().when(student.getStudentId()).thenReturn(studentId);
        return student;
    }

private Course mockCourse(String code, String title, int credits, int sectionCount) {
    Course course = mock(Course.class);
    lenient().when(course.getCourseCode()).thenReturn(code);
    lenient().when(course.getTitle()).thenReturn(title);
    lenient().when(course.getCredits()).thenReturn(credits);

    if (sectionCount < 0) {
        lenient().when(course.getSections()).thenReturn(null);
    } else {
        Set<Section> sections = new HashSet<>();
        for (int i = 0; i < sectionCount; i++) {
            sections.add(mock(Section.class));
        }
        lenient().when(course.getSections()).thenReturn(sections);
    }

    return course;
}

    private RegistrationPeriod mockPeriod(int id, int cohort, LocalDateTime start, LocalDateTime end) {
        RegistrationPeriod period = mock(RegistrationPeriod.class);
        when(period.getPeriodId()).thenReturn(id);
        when(period.getCohort()).thenReturn(cohort);
        when(period.getStartDateTime()).thenReturn(start);
        when(period.getEndDateTime()).thenReturn(end);
        return period;
    }

    // ---------------------------
    // run(), welcome, help, quit, EOF, error catch
    // ---------------------------

    @Test
    void run_shouldExitImmediatelyOnEof() throws Exception {
        when(sessionStore.load()).thenReturn(Optional.empty());
        setInput("");

        runner.run();

        String out = output();
        assertTrue(out.contains("Course Registration CLI"));
        assertTrue(out.contains("Type help to list commands."));
        assertTrue(out.contains("CLI session closed."));
    }

    @Test
    void run_shouldRestoreSession_printHelp_skipBlank_unknownCommand_andQuit() throws Exception {
        CliSession restored = new CliSession("student1", CliRole.STUDENT);
        Student student = mockStudent("student1", "pwd", 1001);

        when(sessionStore.load()).thenReturn(Optional.of(restored));
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        setInput("\nhelp\nwhoami\nunknown-cmd\nquit\n");
        runner.run();

        String out = output();
        assertTrue(out.contains("Restored session for student1 (STUDENT)."));
        assertTrue(out.contains("Available commands:"));
        assertTrue(out.contains("student1 (STUDENT)"));
        assertTrue(out.contains("Unknown command. Type help to see available commands."));
        assertTrue(out.contains("CLI session closed."));
    }

    @Test
    void run_shouldCatchCommandExceptionAndContinue() throws Exception {
        when(sessionStore.load()).thenReturn(Optional.empty());
        setInput("add-section 1\nquit\n");

        runner.run();

        String out = output();
        assertTrue(out.contains("ERROR: Please login first"));
        assertTrue(out.contains("CLI session closed."));
    }

    // ---------------------------
    // loadSavedSession branches
    // ---------------------------

    @Test
    void loadSavedSession_shouldDoNothingWhenNoSession() {
        when(sessionStore.load()).thenReturn(Optional.empty());

        invokeLoadSavedSession();

        assertNull(ReflectionTestUtils.getField(runner, "activeSession"));
    }

    @Test
    void loadSavedSession_shouldRestoreAdminWhenValid() {
        CliSession saved = new CliSession("admin1", CliRole.ADMIN);
        Admin admin = mockAdmin("admin1", "enc", 1, "Admin One");

        when(sessionStore.load()).thenReturn(Optional.of(saved));
        when(adminRepository.findByUserEID("admin1")).thenReturn(Optional.of(admin));

        invokeLoadSavedSession();

        Object session = ReflectionTestUtils.getField(runner, "activeSession");
        assertNotNull(session);
    }

    @Test
    void loadSavedSession_shouldRestoreStudentWhenValid() {
        CliSession saved = new CliSession("student1", CliRole.STUDENT);
        Student student = mockStudent("student1", "enc", 1001);

        when(sessionStore.load()).thenReturn(Optional.of(saved));
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        invokeLoadSavedSession();

        Object session = ReflectionTestUtils.getField(runner, "activeSession");
        assertNotNull(session);
    }

    @Test
    void loadSavedSession_shouldClearWhenAdminInvalid() throws IOException {
        CliSession saved = new CliSession("adminX", CliRole.ADMIN);

        when(sessionStore.load()).thenReturn(Optional.of(saved));
        when(adminRepository.findByUserEID("adminX")).thenReturn(Optional.empty());

        invokeLoadSavedSession();

        verify(sessionStore).clear();
        assertNull(ReflectionTestUtils.getField(runner, "activeSession"));
    }

    @Test
    void loadSavedSession_shouldIgnoreIOExceptionWhenClearingInvalidStudentSession() throws IOException {
        CliSession saved = new CliSession("studentX", CliRole.STUDENT);

        when(sessionStore.load()).thenReturn(Optional.of(saved));
        when(studentRepository.findByUserEID("studentX")).thenReturn(Optional.empty());
        doThrow(new IOException("boom")).when(sessionStore).clear();

        assertDoesNotThrow(this::invokeLoadSavedSession);
        assertNull(ReflectionTestUtils.getField(runner, "activeSession"));
    }

    // ---------------------------
    // login / logout / whoami / passwordMatches
    // ---------------------------

    @Test
    void handleLogin_shouldLoginAsAdminAndLogout() throws Exception {
        Admin admin = mockAdmin("admin1", "encoded", 7, "Admin");
        when(adminRepository.findByUserEID("admin1")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        invokeHandleLine("login admin1 secret");
        invokeHandleLine("whoami");
        invokeHandleLine("logout");
        invokeHandleLine("whoami");

        String out = output();
        assertTrue(out.contains("Logged in as ADMIN: admin1"));
        assertTrue(out.contains("admin1 (ADMIN)"));
        assertTrue(out.contains("Logged out."));
        assertTrue(out.contains("No active session."));
        verify(sessionStore).save(any(CliSession.class));
        verify(sessionStore).clear();
    }

    @Test
    void handleLogin_shouldLoginAsStudent() throws Exception {
        Student student = mockStudent("student1", "encoded", 1001);

        when(adminRepository.findByUserEID("student1")).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        invokeHandleLine("login student1 secret");

        String out = output();
        assertTrue(out.contains("Logged in as STUDENT: student1"));
        verify(sessionStore).save(any(CliSession.class));
    }

    @Test
    void handleLogin_shouldUsePlaintextFallbackWhenEncoderThrows() {
        when(passwordEncoder.matches("plain", "plain")).thenThrow(new IllegalArgumentException("bad format"));

        assertTrue(invokePasswordMatches("plain", "plain"));
        assertFalse(invokePasswordMatches(null, "plain"));
        assertFalse(invokePasswordMatches("plain", null));
    }

    @Test
    void handleLogin_shouldRejectInvalidCredentialsAndBadUsage() {
        when(adminRepository.findByUserEID("u1")).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID("u1")).thenReturn(Optional.empty());

        Exception ex1 = assertThrows(Exception.class, () -> invokeHandleLine("login u1 p1"));
        assertTrue(ex1.getMessage().contains("Invalid credentials"));

        Exception ex2 = assertThrows(Exception.class, () -> invokeHandleLine("login onlyOneArg"));
        assertTrue(ex2.getMessage().contains("Usage: login <userEID> <password>"));
    }

    // ---------------------------
    // list-courses
    // ---------------------------

    @Test
    void listCourses_shouldHandleEmptyAndNonEmptyLists() {
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());
        invokeHandleLine("list-courses");
        assertTrue(output().contains("No courses found."));

        outContent.reset();

        Course c1 = mockCourse("CS101", "Intro Programming", 3, 2);
        Course c2 = mockCourse("CS102", "Data Structures", 4, -1);
        when(courseService.getAllCourses()).thenReturn(List.of(c1, c2));

        invokeHandleLine("list-courses");

        String out = output();
        assertTrue(out.contains("CS101 | Intro Programming | credits=3 | sections=2"));
        assertTrue(out.contains("CS102 | Data Structures | credits=4 | sections=0"));
    }

    // ---------------------------
    // student commands
    // ---------------------------

    @Test
    void addDropJoinWaitlist_shouldCallRegistrationService() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        invokeHandleLine("add-section 11");
        invokeHandleLine("drop-section 12");
        invokeHandleLine("join-waitlist 13");

        verify(registrationService).addSection(eq(1001), eq(11), any(LocalDateTime.class));
        verify(registrationService).dropSection(eq(1001), eq(12), any(LocalDateTime.class));
        verify(registrationService).waitListSection(eq(1001), eq(13), any(LocalDateTime.class));

        String out = output();
        assertTrue(out.contains("Registration added."));
        assertTrue(out.contains("Registration dropped."));
        assertTrue(out.contains("Added to waitlist."));
    }

    @Test
    void studentCommands_shouldValidateUsageAndRole() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Exception ex1 = assertThrows(Exception.class, () -> invokeHandleLine("add-section"));
        assertTrue(ex1.getMessage().contains("Usage: add-section <sectionId>"));

        Exception ex2 = assertThrows(Exception.class, () -> invokeHandleLine("drop-section abc"));
        assertTrue(ex2.getMessage().contains("Invalid integer for sectionId"));

        setAdminSession("admin1");
        Exception ex3 = assertThrows(Exception.class, () -> invokeHandleLine("join-waitlist 1"));
        assertTrue(ex3.getMessage().contains("This command requires STUDENT role"));
    }

    @Test
    void exportTimetable_shouldSupportDefaultAndCustomPath() throws Exception {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Path generated1 = tempDir.resolve("generated1.txt");
        Files.writeString(generated1, "hello");
        when(timetableService.exportTimetable(1001)).thenReturn(generated1);

        invokeHandleLine("export-timetable");

        Path defaultOutput = Path.of("student-1001-timetable.txt").toAbsolutePath();
        assertTrue(Files.exists(defaultOutput));
        assertFalse(Files.exists(generated1));
        assertTrue(output().contains("Timetable exported to " + defaultOutput));
        Files.deleteIfExists(defaultOutput);

        outContent.reset();

        Path generated2 = tempDir.resolve("generated2.txt");
        Files.writeString(generated2, "world");
        when(timetableService.exportTimetable(1001)).thenReturn(generated2);

        Path custom = tempDir.resolve("my-timetable.txt");
        invokeHandleLine("export-timetable \"" + custom.toString() + "\"");

        assertTrue(Files.exists(custom));
        assertFalse(Files.exists(generated2));
        assertTrue(output().contains("Timetable exported to " + custom.toAbsolutePath()));
    }

    @Test
    void exportTimetable_shouldValidateUsage() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Exception ex = assertThrows(Exception.class,
                () -> invokeHandleLine("export-timetable a b"));
        assertTrue(ex.getMessage().contains("Usage: export-timetable [outputPath]"));
    }

    @Test
    void requireStudent_shouldFailWhenStudentAccountNotFound() {
        setStudentSession("ghost");
        when(studentRepository.findByUserEID("ghost")).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> invokeHandleLine("add-section 1"));
        assertTrue(ex.getMessage().contains("Student account not found"));
    }

    // ---------------------------
    // admin user commands
    // ---------------------------

    @Test
    void adminUserCommands_shouldListCreateModifyAndRemoveUsers() {
        setAdminSession("admin1");

        Admin a1 = mockAdmin("adminA", "p", 1, "Alice");
        Admin created = mockAdmin("adminB", "p", 2, "Bob");
        Admin updated = mockAdmin("adminC", "p", 3, "Charlie");

        when(administrativeService.listUsers()).thenReturn(Collections.emptyList(), List.of(a1));
        when(administrativeService.createUser(any(AdminUserRequest.class))).thenReturn(created);
        when(administrativeService.modifyUser(eq(3), any(AdminUserRequest.class))).thenReturn(updated);

        invokeHandleLine("admin-list-users");
        assertTrue(output().contains("No admin users found."));

        outContent.reset();
        invokeHandleLine("admin-list-users");
        invokeHandleLine("admin-create-user adminB \"Bob\" secret");
        invokeHandleLine("admin-modify-user 3 adminC \"Charlie\" newpass");
        invokeHandleLine("admin-remove-user 4");

        String out = output();
        assertTrue(out.contains("1 | adminA | Alice"));
        assertTrue(out.contains("Created admin user with staffId=2"));
        assertTrue(out.contains("Updated admin user 3"));
        assertTrue(out.contains("Removed admin user 4"));

        ArgumentCaptor<AdminUserRequest> createCaptor = ArgumentCaptor.forClass(AdminUserRequest.class);
        verify(administrativeService).createUser(createCaptor.capture());
        assertEquals("adminB", createCaptor.getValue().getUserEID());
        assertEquals("Bob", createCaptor.getValue().getName());
        assertEquals("secret", createCaptor.getValue().getPassword());

        ArgumentCaptor<AdminUserRequest> modifyCaptor = ArgumentCaptor.forClass(AdminUserRequest.class);
        verify(administrativeService).modifyUser(eq(3), modifyCaptor.capture());
        assertEquals("adminC", modifyCaptor.getValue().getUserEID());
        assertEquals("Charlie", modifyCaptor.getValue().getName());
        assertEquals("newpass", modifyCaptor.getValue().getPassword());
    }

    @Test
    void adminModifyUser_shouldAlsoWorkWithoutPassword() {
        setAdminSession("admin1");
        Admin updated = mockAdmin("adminNoPwd", "p", 5, "NoPwd");
        when(administrativeService.modifyUser(eq(5), any(AdminUserRequest.class))).thenReturn(updated);

        invokeHandleLine("admin-modify-user 5 adminNoPwd \"NoPwd\"");

        ArgumentCaptor<AdminUserRequest> captor = ArgumentCaptor.forClass(AdminUserRequest.class);
        verify(administrativeService).modifyUser(eq(5), captor.capture());
        assertNull(captor.getValue().getPassword());
        assertTrue(output().contains("Updated admin user 5"));
    }

    @Test
    void adminUserCommands_shouldValidateUsageAndRole() {
        Exception ex1 = assertThrows(Exception.class, () -> invokeHandleLine("admin-list-users"));
        assertTrue(ex1.getMessage().contains("Please login first"));

        setStudentSession("student1");
        Exception ex2 = assertThrows(Exception.class, () -> invokeHandleLine("admin-create-user a b c"));
        assertTrue(ex2.getMessage().contains("This command requires ADMIN role"));

        setAdminSession("admin1");
        Exception ex3 = assertThrows(Exception.class, () -> invokeHandleLine("admin-create-user a b"));
        assertTrue(ex3.getMessage().contains("Usage: admin-create-user <userEID> <name> <password>"));

        Exception ex4 = assertThrows(Exception.class, () -> invokeHandleLine("admin-remove-user abc"));
        assertTrue(ex4.getMessage().contains("Invalid integer for staffId"));
    }

    // ---------------------------
    // admin course commands
    // ---------------------------

    @Test
    void adminCourseCommands_shouldCreateModifyAndRemoveCourse() {
        setAdminSession("admin1");

        Course created = mockCourse("CS211", "OOP", 3, 1);
        Course updated = mockCourse("CS211", "Advanced OOP", 4, 2);

        when(courseService.getCourse("CS211")).thenReturn(null, created);
        when(administrativeService.createCourse(any(AdminCourseRequest.class))).thenReturn(created);
        when(administrativeService.modifyCourse(any(AdminCourseRequest.class))).thenReturn(updated);

        invokeHandleLine("admin-create-course --code CS211 --title Object Oriented Programming --credits 3 --description core subject --prereq CS101,CS102 --exclusive CS999, CS998");
        invokeHandleLine("admin-create-course --code CS211 --title Advanced OOP --credits 4 --description updated --prereq CS101 --exclusive CS998");
        invokeHandleLine("admin-remove-course CS211");

        String out = output();
        assertTrue(out.contains("Created course CS211"));
        assertTrue(out.contains("Updated course CS211"));
        assertTrue(out.contains("Removed course CS211"));

        ArgumentCaptor<AdminCourseRequest> createCaptor = ArgumentCaptor.forClass(AdminCourseRequest.class);
        verify(administrativeService).createCourse(createCaptor.capture());
        AdminCourseRequest createReq = createCaptor.getValue();
        assertEquals("CS211", createReq.getCourseCode());
        assertEquals("Object Oriented Programming", createReq.getTitle());
        assertEquals(3, createReq.getCredits());
        assertEquals("core subject", createReq.getDescription());
        assertEquals(Set.of("CS101", "CS102"), createReq.getPrerequisiteCourseCodes());
        assertEquals(Set.of("CS999", "CS998"), createReq.getExclusiveCourseCodes());

        ArgumentCaptor<AdminCourseRequest> modifyCaptor = ArgumentCaptor.forClass(AdminCourseRequest.class);
        verify(administrativeService).modifyCourse(modifyCaptor.capture());
        AdminCourseRequest modifyReq = modifyCaptor.getValue();
        assertEquals("CS211", modifyReq.getCourseCode());
        assertEquals("Advanced OOP", modifyReq.getTitle());
        assertEquals(4, modifyReq.getCredits());
        assertEquals("updated", modifyReq.getDescription());
        assertEquals(Set.of("CS101"), modifyReq.getPrerequisiteCourseCodes());
        assertEquals(Set.of("CS998"), modifyReq.getExclusiveCourseCodes());
    }

    @Test
    void adminCourseCommands_shouldValidateMissingRequiredFields() {
        setAdminSession("admin1");

        Exception ex1 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-course --title T --credits 3"));
        assertTrue(ex1.getMessage().contains("--code is required"));

        Exception ex2 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-course --code CS100 --credits 3"));
        assertTrue(ex2.getMessage().contains("--title is required when creating a new course"));

        Exception ex3 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-course --code CS100 --title T"));
        assertTrue(ex3.getMessage().contains("--credits is required when creating a new course"));

        Course existing = mockCourse("CS100", "Old", 3, 0);
        when(courseService.getCourse("CS100")).thenReturn(existing);
        Exception ex4 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-course --code CS100 --credits x"));
        assertTrue(ex4.getMessage().contains("Invalid integer for credits"));

        Exception ex5 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-remove-course"));
        assertTrue(ex5.getMessage().contains("Usage: admin-remove-course <courseCode>"));
    }

        @Test
        void adminCourseCommands_shouldRejectUnknownOptions() {
        setAdminSession("admin1");

        Exception ex1 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-course --code CS211 --title Object Oriented Programming --credits 3 --term 2026A"));
        assertTrue(ex1.getMessage().contains("Unknown option(s) for admin-create-course: --term"));

        Exception ex2 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-course --code CS211 --wrongField abc"));
        assertTrue(ex2.getMessage().contains("Unknown option(s) for admin-create-course: --wrongfield"));
        }

    // ---------------------------
    // admin period commands
    // ---------------------------

    @Test
    void adminPeriodCommands_shouldListCreateAndDeletePeriods() {
        setAdminSession("admin1");

        RegistrationPeriod p1 = mockPeriod(
            1, 2024,
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 10, 18, 0));

        RegistrationPeriod p2 = mockPeriod(
            2, 2025,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 10, 18, 0));

        when(administrativeService.listRegistrationPeriods(2024)).thenReturn(List.of(p1));
        when(administrativeService.listRegistrationPeriods(null)).thenReturn(List.of(p1, p2));

        invokeHandleLine("admin-list-periods --cohort 2024");
        invokeHandleLine("admin-list-periods");
        invokeHandleLine("admin-create-period --cohort 2025 --start 2025-02-01T09:00 --end 2025-02-10T18:00");
        invokeHandleLine("admin-delete-period 2");

        String out = output();
        assertTrue(out.contains("1 | cohort=2024 | 2025-01-01T09:00 -> 2025-01-10T18:00"));
        assertTrue(out.contains("2 | cohort=2025 | 2025-02-01T09:00 -> 2025-02-10T18:00"));
        assertTrue(out.contains("Registration period created."));
        assertTrue(out.contains("Registration period 2 deleted."));

        ArgumentCaptor<AdminPeriodRequest> captor = ArgumentCaptor.forClass(AdminPeriodRequest.class);
        verify(administrativeService).createRegistrationPeriod(captor.capture());
        AdminPeriodRequest req = captor.getValue();
        assertEquals(2025, req.getCohort());
        assertEquals(LocalDateTime.of(2025, 2, 1, 9, 0), req.getStartDate());
        assertEquals(LocalDateTime.of(2025, 2, 10, 18, 0), req.getEndDate());
    }

    @Test
    void adminPeriodCommands_shouldHandleEmptyAndInvalidUsage() {
        setAdminSession("admin1");

        when(administrativeService.listRegistrationPeriods(null)).thenReturn(Collections.emptyList());
        invokeHandleLine("admin-list-periods");
        assertTrue(output().contains("No registration periods found."));

        Exception ex1 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-list-periods --cohort abc"));
        assertTrue(ex1.getMessage().contains("Invalid integer for cohort"));

        Exception ex2 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-period --start 2025-01-01T09:00 --end 2025-01-02T09:00"));
        assertTrue(ex2.getMessage().contains("Usage: admin-create-period"));

        Exception ex4 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-period --cohort 2024 --end 2025-01-02T09:00"));
        assertTrue(ex4.getMessage().contains("Usage: admin-create-period"));

        Exception ex5 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-period --cohort 2024 --start 2025-01-01T09:00"));
        assertTrue(ex5.getMessage().contains("Usage: admin-create-period"));

        Exception ex6 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-period --cohort 2024 --start bad-date --end 2025-01-02T09:00"));
        assertTrue(ex6.getMessage().contains("Invalid date-time for start"));

        Exception ex7 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-delete-period"));
        assertTrue(ex7.getMessage().contains("Usage: admin-delete-period <periodId>"));

        Exception ex8 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-delete-period bad"));
        assertTrue(ex8.getMessage().contains("Invalid integer for periodId"));
    }

    // ---------------------------
    // unknown command
    // ---------------------------

    @Test
    void handleLine_shouldPrintUnknownCommand() {
        invokeHandleLine("something-unsupported");
        assertTrue(output().contains("Unknown command. Type help to see available commands."));
    }

@Test
void dropSection_shouldThrowWhenArgumentCountIsWrong() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Exception ex = assertThrows(Exception.class, () -> invokeHandleLine("drop-section"));
    assertTrue(ex.getMessage().contains("Usage: drop-section <sectionId>"));
}

@Test
void joinWaitlist_shouldThrowWhenArgumentCountIsWrong() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Exception ex = assertThrows(Exception.class, () -> invokeHandleLine("join-waitlist"));
    assertTrue(ex.getMessage().contains("Usage: join-waitlist <sectionId>"));
}

@Test
void adminModifyUser_shouldThrowWhenArgumentCountIsInvalid() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-modify-user 1 onlyTwoArgs"));
    assertTrue(ex.getMessage().contains(
            "Usage: admin-modify-user <staffId> <userEID> <name> [password]"));
}

@Test
void adminRemoveUser_shouldThrowWhenArgumentCountIsWrong() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class, () -> invokeHandleLine("admin-remove-user"));
    assertTrue(ex.getMessage().contains("Usage: admin-remove-user <staffId>"));
}

@Test
void adminUpsertCourse_shouldCoverSplitCsvNullBranch() {
    setAdminSession("admin1");

    Course updated = mockCourse("CS100", "Title", 3, 0);
    when(courseService.getCourse("CS100")).thenReturn(updated);
    when(administrativeService.modifyCourse(any(AdminCourseRequest.class))).thenReturn(updated);

    invokeHandleLine("admin-create-course --code CS100 --title \"New Title\"");

    ArgumentCaptor<AdminCourseRequest> captor = ArgumentCaptor.forClass(AdminCourseRequest.class);
    verify(administrativeService).modifyCourse(captor.capture());

    assertNull(captor.getValue().getPrerequisiteCourseCodes());
    assertNull(captor.getValue().getExclusiveCourseCodes());
}

@Test
void adminUpsertCourse_shouldCoverSplitCsvEmptySetBranch() {
    setAdminSession("admin1");

    Course updated = mockCourse("CS101", "Title", 3, 0);
    when(courseService.getCourse("CS101")).thenReturn(updated);
    when(administrativeService.modifyCourse(any(AdminCourseRequest.class))).thenReturn(updated);

    invokeHandleLine("admin-create-course --code CS101 --prereq \" , , \" --exclusive \" , , \"");

    ArgumentCaptor<AdminCourseRequest> captor = ArgumentCaptor.forClass(AdminCourseRequest.class);
    verify(administrativeService).modifyCourse(captor.capture());

    assertNotNull(captor.getValue().getPrerequisiteCourseCodes());
    assertTrue(captor.getValue().getPrerequisiteCourseCodes().isEmpty());
    assertNotNull(captor.getValue().getExclusiveCourseCodes());
    assertTrue(captor.getValue().getExclusiveCourseCodes().isEmpty());
}
@Test
void handleLine_shouldReturnWhenTokenListIsEmpty() {
    assertDoesNotThrow(() -> invokeHandleLine(""));
}

@Test
void studentPlanCommands_shouldListCreateRemoveAddEntryRemoveEntryAndReorder() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    RegistrationPlan plan = new RegistrationPlan();
    plan.setPlanId(11);
    plan.setPriority(1);
    plan.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
    plan.setApplySummary("Awaiting period start");

    Section section = new Section();
    section.setSectionId(88);

    PlanEntry entry = new PlanEntry();
    entry.setEntryId(22);
    entry.setSection(section);
    entry.setEntryType(PlanEntry.EntryType.SELECTED);
    entry.setStatus(PlanEntry.EntryStatus.PENDING);
    entry.setJoinWaitlistOnAddFailure(true);
    plan.addEntry(entry);

    when(registrationPlanService.getPlanSet(1001)).thenReturn(List.of(plan));
    when(registrationPlanService.createPlan(1001, 2)).thenReturn(plan);
    when(registrationPlanService.addEntry(11, 88, PlanEntry.EntryType.SELECTED, true)).thenReturn(entry);
    when(registrationPlanService.reorderPlans(1001, List.of(11))).thenReturn(List.of(plan));

    invokeHandleLine("list-plans");
    invokeHandleLine("create-plan 2");
    invokeHandleLine("add-plan-entry 11 88 selected true");
    invokeHandleLine("remove-plan-entry 11 22");
    invokeHandleLine("reorder-plans 11");
    invokeHandleLine("remove-plan 11");

    verify(registrationPlanService).getPlanSet(1001);
    verify(registrationPlanService).createPlan(1001, 2);
    verify(registrationPlanService).addEntry(11, 88, PlanEntry.EntryType.SELECTED, true);
    verify(registrationPlanService).removeEntry(11, 22);
    verify(registrationPlanService).reorderPlans(1001, List.of(11));
    verify(registrationPlanService).removePlan(11);

    String out = output();
    assertTrue(out.contains("planId=11 | priority=1"));
    assertTrue(out.contains("Created plan 11 with priority=1"));
    assertTrue(out.contains("Added plan entry 22"));
    assertTrue(out.contains("Removed plan entry 22"));
    assertTrue(out.contains("Plans reordered."));
    assertTrue(out.contains("Removed plan 11"));
}

@Test
void studentPlanCommands_shouldValidateUsageAndRole() {
    setAdminSession("admin1");
    Exception roleEx = assertThrows(Exception.class, () -> invokeHandleLine("list-plans"));
    assertTrue(roleEx.getMessage().contains("This command requires STUDENT role"));

    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Exception usageEx1 = assertThrows(Exception.class, () -> invokeHandleLine("create-plan 1 2"));
    assertTrue(usageEx1.getMessage().contains("Usage: create-plan [priority]"));

    Exception usageEx2 = assertThrows(Exception.class, () -> invokeHandleLine("remove-plan"));
    assertTrue(usageEx2.getMessage().contains("Usage: remove-plan <planId>"));

    Exception usageEx3 = assertThrows(Exception.class, () -> invokeHandleLine("add-plan-entry 1 2 badType"));
    assertTrue(usageEx3.getMessage().contains("Invalid entry type. Use SELECTED or WAITLIST"));

    Exception usageEx4 = assertThrows(Exception.class, () -> invokeHandleLine("remove-plan-entry 1"));
    assertTrue(usageEx4.getMessage().contains("Usage: remove-plan-entry <planId> <entryId>"));

    Exception usageEx5 = assertThrows(Exception.class, () -> invokeHandleLine("reorder-plans"));
    assertTrue(usageEx5.getMessage().contains("Usage: reorder-plans <planIdCsv>"));
}
}



