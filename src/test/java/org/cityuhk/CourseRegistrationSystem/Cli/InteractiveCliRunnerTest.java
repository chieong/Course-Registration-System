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
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminSectionService;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.InstructorUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.StudentUserRequest;
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
    private RegistrationRecordRepository registrationRecordRepository;
    @Mock
    private WaitlistRecordRepository waitlistRecordRepository;
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

    private void setInstructorSession(String eid) {
        ReflectionTestUtils.setField(runner, "activeSession", new CliSession(eid, CliRole.INSTRUCTOR));
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

    private Instructor mockInstructor(String eid, String pwd, int staffId, String userName) {
        Instructor instructor = mock(Instructor.class);
        lenient().when(instructor.getUserEID()).thenReturn(eid);
        lenient().when(instructor.getPassword()).thenReturn(pwd);
        lenient().when(instructor.getStaffId()).thenReturn(staffId);
        lenient().when(instructor.getUserName()).thenReturn(userName);
        return instructor;
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

        CliSession active = (CliSession) ReflectionTestUtils.getField(runner, "activeSession");
        assertNotNull(active);
        assertEquals("admin1", active.getUserEid());
        assertEquals(CliRole.ADMIN, active.getRole());
    }

    @Test
    void loadSavedSession_shouldRestoreStudentWhenValid() {
        CliSession saved = new CliSession("student1", CliRole.STUDENT);
        Student student = mockStudent("student1", "enc", 1001);

        when(sessionStore.load()).thenReturn(Optional.of(saved));
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        invokeLoadSavedSession();

        CliSession active = (CliSession) ReflectionTestUtils.getField(runner, "activeSession");
        assertNotNull(active);
        assertEquals("student1", active.getUserEid());
        assertEquals(CliRole.STUDENT, active.getRole());
    }

    @Test
    void loadSavedSession_shouldRestoreInstructorWhenValid() {
        CliSession saved = new CliSession("ins1", CliRole.INSTRUCTOR);
        Instructor instructor = mockInstructor("ins1", "enc", 2001, "Instructor One");

        when(sessionStore.load()).thenReturn(Optional.of(saved));
        when(instructorRepository.findByUserEID("ins1")).thenReturn(Optional.of(instructor));

        invokeLoadSavedSession();

        CliSession active = (CliSession) ReflectionTestUtils.getField(runner, "activeSession");
        assertNotNull(active);
        assertEquals("ins1", active.getUserEid());
        assertEquals(CliRole.INSTRUCTOR, active.getRole());
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
    void loadSavedSession_shouldClearWhenInstructorInvalid() throws IOException {
        CliSession saved = new CliSession("insX", CliRole.INSTRUCTOR);

        when(sessionStore.load()).thenReturn(Optional.of(saved));
        when(instructorRepository.findByUserEID("insX")).thenReturn(Optional.empty());

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
    void handleLogin_shouldLoginAsInstructor() throws Exception {
        Instructor instructor = mockInstructor("ins1", "encoded", 2001, "Instructor One");

        when(adminRepository.findByUserEID("ins1")).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID("ins1")).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID("ins1")).thenReturn(Optional.of(instructor));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        invokeHandleLine("login ins1 secret");

        String out = output();
        assertTrue(out.contains("Logged in as INSTRUCTOR: ins1"));
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
        when(instructorRepository.findByUserEID("u1")).thenReturn(Optional.empty());

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
    // view-master-schedule
    // ---------------------------

    @Test
    void viewMasterSchedule_shouldPrintCourseSectionInstructorAndCapacityInfo() {
        setStudentSession("student1");
        Student student = mockStudent("student1", "pwd", 1001);
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Course course = mock(Course.class);
        when(course.getCourseCode()).thenReturn("CS211");
        when(course.getTitle()).thenReturn("Object Oriented Programming");
        when(course.getCredits()).thenReturn(3);
        when(course.getDescription()).thenReturn("Core OOP course");

        Course prereq = mock(Course.class);
        when(prereq.getCourseCode()).thenReturn("CS101");

        Course exclusive = mock(Course.class);
        when(exclusive.getCourseCode()).thenReturn("CS999");

        when(course.getPrerequisiteCourses()).thenReturn(Set.of(prereq));
        when(course.getExclusiveCourses()).thenReturn(Set.of(exclusive));

        Section section = mock(Section.class);
        when(section.getSectionId()).thenReturn(11);
        when(section.getType()).thenReturn(Section.Type.LECTURE);
        when(section.getStartTime()).thenReturn(LocalDateTime.of(2025, 9, 1, 9, 0));
        when(section.getEndTime()).thenReturn(LocalDateTime.of(2025, 9, 1, 10, 50));
        when(section.getVenue()).thenReturn("LT-1");

        Instructor instructor = mockInstructor("ins1", "pwd", 7, "Dr Chan");
        when(section.getInstructors()).thenReturn(Set.of(instructor));

        when(course.getSections()).thenReturn(Set.of(section));
        when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));

        when(registrationRecordRepository.countEnrolled(11)).thenReturn(20);
        when(waitlistRecordRepository.countWaitlisted(11)).thenReturn(5);
        when(section.getEnrollCapacity()).thenReturn(30);
        when(section.getWaitlistCapacity()).thenReturn(10);

        invokeHandleLine("view-master-schedule");

        String out = output();
        assertTrue(out.contains("MASTER CLASS SCHEDULE"));
        assertTrue(out.contains("COURSE: CS211 - Object Oriented Programming"));
        assertTrue(out.contains("Credits: 3"));
        assertTrue(out.contains("Description: Core OOP course"));
        assertTrue(out.contains("Prerequisites: CS101"));
        assertTrue(out.contains("Exclusive Courses: CS999"));
        assertTrue(out.contains("Section ID: 11"));
        assertTrue(out.contains("Type: LECTURE"));
        assertTrue(out.contains("Venue: LT-1"));
        assertTrue(out.contains("Instructors: Dr Chan"));
        assertTrue(out.contains("Enrollment: 20/30 (Available: 10)"));
        assertTrue(out.contains("Waitlist: 5/10 (Available: 5)"));
    }

    @Test
    void viewMasterSchedule_shouldHandleNoCourses() {
        setAdminSession("admin1");
        when(courseService.getAllCoursesWithAllData()).thenReturn(Collections.emptyList());

        invokeHandleLine("view-master-schedule");

        assertTrue(output().contains("No courses found."));
    }

    @Test
    void viewMasterSchedule_shouldPrintNoSectionsAndNAValues() {
        setStudentSession("student1");
        Student student = mockStudent("student1", "pwd", 1001);
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Course course = mock(Course.class);
        when(course.getCourseCode()).thenReturn("CS100");
        when(course.getTitle()).thenReturn("Title");
        when(course.getCredits()).thenReturn(3);
        when(course.getDescription()).thenReturn("");
        when(course.getPrerequisiteCourses()).thenReturn(Collections.emptySet());
        when(course.getExclusiveCourses()).thenReturn(Collections.emptySet());
        when(course.getSections()).thenReturn(Collections.emptySet());

        when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));

        invokeHandleLine("view-master-schedule");

        String out = output();
        assertTrue(out.contains("COURSE: CS100 - Title"));
        assertTrue(out.contains("[No sections available]"));
    }

    // ---------------------------
    // student commands
    // ---------------------------

    @Test
    void addDropJoinWaitlistAndDropWaitlist_shouldCallRegistrationService() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        invokeHandleLine("add-section 11");
        invokeHandleLine("drop-section 12");
        invokeHandleLine("join-waitlist 13");
        invokeHandleLine("drop-waitlist 14");

        verify(registrationService).addSection(eq(1001), eq(11), any(LocalDateTime.class));
        verify(registrationService).dropSection(eq(1001), eq(12), any(LocalDateTime.class));
        verify(registrationService).waitListSection(eq(1001), eq(13), any(LocalDateTime.class));
        verify(registrationService).dropWaitlist(eq(1001), eq(14));

        String out = output();
        assertTrue(out.contains("Registration added."));
        assertTrue(out.contains("Registration dropped."));
        assertTrue(out.contains("Added to waitlist."));
        assertTrue(out.contains("Removed from waitlist."));
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

        Exception ex3 = assertThrows(Exception.class, () -> invokeHandleLine("drop-waitlist"));
        assertTrue(ex3.getMessage().contains("Usage: drop-waitlist <sectionId>"));

        Exception ex4 = assertThrows(Exception.class, () -> invokeHandleLine("drop-waitlist bad"));
        assertTrue(ex4.getMessage().contains("Invalid integer for sectionId"));

        setAdminSession("admin1");
        Exception ex5 = assertThrows(Exception.class, () -> invokeHandleLine("join-waitlist 1"));
        assertTrue(ex5.getMessage().contains("This command requires STUDENT role"));
    }

    @Test
    void exportTimetable_shouldSupportStudentDefaultAndCustomPath() throws Exception {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Path generated1 = tempDir.resolve("generated1.txt");
        Files.writeString(generated1, "hello");
        when(timetableService.exportStudentTimetable(1001)).thenReturn(generated1);

        invokeHandleLine("export-timetable");

        Path defaultOutput = Path.of("student-1001-timetable.txt").toAbsolutePath();
        assertTrue(Files.exists(defaultOutput));
        assertFalse(Files.exists(generated1));
        assertTrue(output().contains("Timetable exported to " + defaultOutput));
        Files.deleteIfExists(defaultOutput);

        outContent.reset();

        Path generated2 = tempDir.resolve("generated2.txt");
        Files.writeString(generated2, "world");
        when(timetableService.exportStudentTimetable(1001)).thenReturn(generated2);

        Path custom = tempDir.resolve("my-timetable.txt");
        invokeHandleLine("export-timetable \"" + custom.toString() + "\"");

        assertTrue(Files.exists(custom));
        assertFalse(Files.exists(generated2));
        assertTrue(output().contains("Timetable exported to " + custom.toAbsolutePath()));
    }

    @Test
    void exportTimetable_shouldSupportInstructorDefaultPath() throws Exception {
        Instructor instructor = mockInstructor("ins1", "pwd", 2001, "Instructor One");
        setInstructorSession("ins1");
        when(instructorRepository.findByUserEID("ins1")).thenReturn(Optional.of(instructor));

        Path generated = tempDir.resolve("instructor-generated.txt");
        Files.writeString(generated, "inst timetable");
        when(timetableService.exportInstructorTimetable(2001)).thenReturn(generated);

        invokeHandleLine("export-timetable");

        Path defaultOutput = Path.of("instructor-2001-timetable.txt").toAbsolutePath();
        assertTrue(Files.exists(defaultOutput));
        assertFalse(Files.exists(generated));
        assertTrue(output().contains("Timetable exported to " + defaultOutput));
        Files.deleteIfExists(defaultOutput);
    }

    @Test
    void exportTimetable_shouldRejectAdminRole() {
        setAdminSession("admin1");

        Exception ex = assertThrows(Exception.class, () -> invokeHandleLine("export-timetable"));
        assertTrue(ex.getMessage().contains("Only Instrucotor and Student can export timetable."));
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

    @Test
    void requireInstructor_shouldFailWhenInstructorAccountNotFound() {
        setInstructorSession("ghost");
        when(instructorRepository.findByUserEID("ghost")).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class,
                () -> ReflectionTestUtils.invokeMethod(runner, "handleExportTimetable", List.of()));
        assertTrue(ex.getMessage().contains("Instructor account not found"));
    }

    // ---------------------------
    // show-timetable
    // IMPORTANT:
    // Current production code requires exactly 1 arg.
    // Therefore tests use "show-timetable dummy".
    // ---------------------------

    @Test
    void showTimetable_shouldWorkForStudent_currentImplementationRequiresOneArgument() throws Exception {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));
        when(timetableService.getStudentTimetableString(1001)).thenReturn("Student timetable");

        invokeHandleLine("show-timetable dummy");

        assertTrue(output().contains("Student timetable"));
    }

    @Test
    void showTimetable_shouldWorkForInstructor_currentImplementationRequiresOneArgument() throws Exception {
        Instructor instructor = mockInstructor("ins1", "pwd", 2001, "Instructor One");
        setInstructorSession("ins1");
        when(instructorRepository.findByUserEID("ins1")).thenReturn(Optional.of(instructor));
        when(timetableService.getStudentTimetableString(2001)).thenReturn("Instructor timetable");

        invokeHandleLine("show-timetable dummy");

        assertTrue(output().contains("Instructor timetable"));
    }

    @Test
    void showTimetable_shouldValidateUsage_currentImplementation() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Exception ex = assertThrows(Exception.class, () -> invokeHandleLine("show-timetable"));
        assertTrue(ex.getMessage().contains("Usage: show-timetable"));
    }

    @Test
    void showTimetable_shouldDoNothingForAdmin_currentImplementation() throws Exception {
        setAdminSession("admin1");

        invokeHandleLine("show-timetable dummy");

        assertEquals("", output());
    }

    // ---------------------------
    // student plan commands
    // ---------------------------

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
    void listPlans_shouldHandleEmptyList() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));
        when(registrationPlanService.getPlanSet(1001)).thenReturn(Collections.emptyList());

        invokeHandleLine("list-plans");

        assertTrue(output().contains("No plans found."));
    }

    @Test
    void listPlans_shouldHandleNullEntriesAndNullSummary() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        RegistrationPlan plan = new RegistrationPlan();
        plan.setPlanId(50);
        plan.setPriority(2);
        plan.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
        plan.setApplySummary(null);
        plan.addEntry(null);

        when(registrationPlanService.getPlanSet(1001)).thenReturn(List.of(plan));

        invokeHandleLine("list-plans");

        String out = output();
        assertTrue(out.contains("planId=50 | priority=2"));
        assertTrue(out.contains("summary=-"));
    }

    @Test
    void createPlan_shouldAllowDefaultPriority() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        RegistrationPlan plan = new RegistrationPlan();
        plan.setPlanId(99);
        plan.setPriority(1);

        when(registrationPlanService.createPlan(1001, null)).thenReturn(plan);

        invokeHandleLine("create-plan");

        verify(registrationPlanService).createPlan(1001, null);
        assertTrue(output().contains("Created plan 99 with priority=1"));
    }

    @Test
    void reorderPlans_shouldRejectEmptyCsv() {
        Student student = mockStudent("student1", "pwd", 1001);
        setStudentSession("student1");
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Exception ex = assertThrows(Exception.class, () -> invokeHandleLine("reorder-plans \", ,\""));
        assertTrue(ex.getMessage().contains("planIdCsv must contain at least one planId"));
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

    // ---------------------------
    // admin student commands
    // ---------------------------

    @Test
    void adminStudentCommands_shouldListCreateModifyAndRemoveStudents() {
        setAdminSession("admin1");

        Student listed = mock(Student.class);
        when(listed.getStudentId()).thenReturn(1001);
        when(listed.getUserEID()).thenReturn("s1");
        when(listed.getUserName()).thenReturn("Student One");
        when(listed.getMajor()).thenReturn("CS");
        when(listed.getDepartment()).thenReturn("CS Dept");

        Student created = mock(Student.class);
        when(created.getStudentId()).thenReturn(1002);

        Student updated = mock(Student.class);
        when(updated.getStudentId()).thenReturn(1003);

        when(administrativeService.listStudents()).thenReturn(Collections.emptyList(), List.of(listed));
        when(administrativeService.createStudent(any(StudentUserRequest.class))).thenReturn(created);
        when(administrativeService.modifyStudent(eq(1003), any(StudentUserRequest.class))).thenReturn(updated);

        invokeHandleLine("admin-list-students");
        assertTrue(output().contains("No students found."));

        outContent.reset();

        invokeHandleLine("admin-list-students");
        invokeHandleLine("admin-create-student s2 \"Student Two\" secret --major CS --dept COMP");
        invokeHandleLine("admin-modify-student 1003 s3 \"Student Three\" newpwd --major SE --dept CSE");
        invokeHandleLine("admin-remove-student 1004");

        String out = output();
        assertTrue(out.contains("1001 | s1 | Student One | major=CS | dept=CS Dept"));
        assertTrue(out.contains("Created student with studentId=1002"));
        assertTrue(out.contains("Updated student 1003"));
        assertTrue(out.contains("Removed student 1004"));

        ArgumentCaptor<StudentUserRequest> createCaptor = ArgumentCaptor.forClass(StudentUserRequest.class);
        verify(administrativeService).createStudent(createCaptor.capture());
        assertEquals("s2", createCaptor.getValue().getUserEID());
        assertEquals("Student Two", createCaptor.getValue().getName());
        assertEquals("secret", createCaptor.getValue().getPassword());
        assertEquals("CS", createCaptor.getValue().getMajor());
        assertEquals("COMP", createCaptor.getValue().getDepartment());

        ArgumentCaptor<StudentUserRequest> modifyCaptor = ArgumentCaptor.forClass(StudentUserRequest.class);
        verify(administrativeService).modifyStudent(eq(1003), modifyCaptor.capture());
        assertEquals("s3", modifyCaptor.getValue().getUserEID());
        assertEquals("Student Three", modifyCaptor.getValue().getName());
        assertEquals("newpwd", modifyCaptor.getValue().getPassword());
        assertEquals("SE", modifyCaptor.getValue().getMajor());
        assertEquals("CSE", modifyCaptor.getValue().getDepartment());
    }

    @Test
    void adminModifyStudent_shouldAlsoWorkWithoutPassword() {
        setAdminSession("admin1");
        Student updated = mock(Student.class);
        when(updated.getStudentId()).thenReturn(1005);
        when(administrativeService.modifyStudent(eq(1005), any(StudentUserRequest.class))).thenReturn(updated);

        invokeHandleLine("admin-modify-student 1005 s5 \"Student Five\" --major CS --dept COMP");

        ArgumentCaptor<StudentUserRequest> captor = ArgumentCaptor.forClass(StudentUserRequest.class);
        verify(administrativeService).modifyStudent(eq(1005), captor.capture());
        assertEquals("s5", captor.getValue().getUserEID());
        assertEquals("Student Five", captor.getValue().getName());
        assertNull(captor.getValue().getPassword());
        assertEquals("CS", captor.getValue().getMajor());
        assertEquals("COMP", captor.getValue().getDepartment());
        assertTrue(output().contains("Updated student 1005"));
    }

    @Test
    void adminListStudents_shouldPrintDashForNullFields() {
        setAdminSession("admin1");

        Student listed = mock(Student.class);
        when(listed.getStudentId()).thenReturn(2001);
        when(listed.getUserEID()).thenReturn("sNull");
        when(listed.getUserName()).thenReturn("No Major");
        when(listed.getMajor()).thenReturn(null);
        when(listed.getDepartment()).thenReturn(" ");

        when(administrativeService.listStudents()).thenReturn(List.of(listed));

        invokeHandleLine("admin-list-students");

        String out = output();
        assertTrue(out.contains("2001 | sNull | No Major | major=- | dept=-"));
    }

    @Test
    void adminStudentCommands_shouldValidateUsage() {
        setAdminSession("admin1");

        Exception ex1 = assertThrows(Exception.class, () -> invokeHandleLine("admin-create-student a b"));
        assertTrue(ex1.getMessage().contains("Usage: admin-create-student"));

        Exception ex2 = assertThrows(Exception.class, () -> invokeHandleLine("admin-modify-student 1001"));
        assertTrue(ex2.getMessage().contains("Usage: admin-modify-student"));

        Exception ex3 = assertThrows(Exception.class, () -> invokeHandleLine("admin-remove-student"));
        assertTrue(ex3.getMessage().contains("Usage: admin-remove-student <studentId>"));
    }

    // ---------------------------
    // admin instructor commands
    // ---------------------------

    @Test
    void adminInstructorCommands_shouldListCreateModifyAndRemoveInstructors() {
        setAdminSession("admin1");

        Instructor listed = mock(Instructor.class);
        when(listed.getStaffId()).thenReturn(201);
        when(listed.getUserEID()).thenReturn("i1");
        when(listed.getUserName()).thenReturn("Instructor One");
        when(listed.getDepartment()).thenReturn("COMP");

        Instructor created = mockInstructor("i2", "pwd", 202, "Instructor Two");
        Instructor updated = mockInstructor("i3", "pwd", 203, "Instructor Three");

        when(administrativeService.listInstructors()).thenReturn(Collections.emptyList(), List.of(listed));
        when(administrativeService.createInstructor(any(InstructorUserRequest.class))).thenReturn(created);
        when(administrativeService.modifyInstructor(eq(203), any(InstructorUserRequest.class))).thenReturn(updated);

        invokeHandleLine("admin-list-instructors");
        assertTrue(output().contains("No instructors found."));

        outContent.reset();

        invokeHandleLine("admin-list-instructors");
        invokeHandleLine("admin-create-instructor i2 \"Instructor Two\" secret --dept CS");
        invokeHandleLine("admin-modify-instructor 203 i3 \"Instructor Three\" newpwd --dept EE");
        invokeHandleLine("admin-remove-instructor 204");

        String out = output();
        assertTrue(out.contains("201 | i1 | Instructor One | dept=COMP"));
        assertTrue(out.contains("Created instructor with staffId=202"));
        assertTrue(out.contains("Updated instructor 203"));
        assertTrue(out.contains("Removed instructor 204"));

        ArgumentCaptor<InstructorUserRequest> createCaptor = ArgumentCaptor.forClass(InstructorUserRequest.class);
        verify(administrativeService).createInstructor(createCaptor.capture());
        assertEquals("i2", createCaptor.getValue().getUserEID());
        assertEquals("Instructor Two", createCaptor.getValue().getName());
        assertEquals("secret", createCaptor.getValue().getPassword());
        assertEquals("CS", createCaptor.getValue().getDepartment());

        ArgumentCaptor<InstructorUserRequest> modifyCaptor = ArgumentCaptor.forClass(InstructorUserRequest.class);
        verify(administrativeService).modifyInstructor(eq(203), modifyCaptor.capture());
        assertEquals("i3", modifyCaptor.getValue().getUserEID());
        assertEquals("Instructor Three", modifyCaptor.getValue().getName());
        assertEquals("newpwd", modifyCaptor.getValue().getPassword());
        assertEquals("EE", modifyCaptor.getValue().getDepartment());
    }

    @Test
    void adminModifyInstructor_shouldAlsoWorkWithoutPassword() {
        setAdminSession("admin1");
        Instructor updated = mockInstructor("i5", "pwd", 205, "Instructor Five");
        when(administrativeService.modifyInstructor(eq(205), any(InstructorUserRequest.class))).thenReturn(updated);

        invokeHandleLine("admin-modify-instructor 205 i5 \"Instructor Five\" --dept COMP");

        ArgumentCaptor<InstructorUserRequest> captor = ArgumentCaptor.forClass(InstructorUserRequest.class);
        verify(administrativeService).modifyInstructor(eq(205), captor.capture());
        assertEquals("i5", captor.getValue().getUserEID());
        assertEquals("Instructor Five", captor.getValue().getName());
        assertNull(captor.getValue().getPassword());
        assertEquals("COMP", captor.getValue().getDepartment());
        assertTrue(output().contains("Updated instructor 205"));
    }

    @Test
    void adminListInstructors_shouldPrintDashForNullDepartment() {
        setAdminSession("admin1");

        Instructor listed = mock(Instructor.class);
        when(listed.getStaffId()).thenReturn(301);
        when(listed.getUserEID()).thenReturn("iNull");
        when(listed.getUserName()).thenReturn("No Dept");
        when(listed.getDepartment()).thenReturn(null);

        when(administrativeService.listInstructors()).thenReturn(List.of(listed));

        invokeHandleLine("admin-list-instructors");

        String out = output();
        assertTrue(out.contains("301 | iNull | No Dept | dept=-"));
    }

    @Test
    void adminInstructorCommands_shouldValidateUsage() {
        setAdminSession("admin1");

        Exception ex1 = assertThrows(Exception.class, () -> invokeHandleLine("admin-create-instructor a b"));
        assertTrue(ex1.getMessage().contains("Usage: admin-create-instructor"));

        Exception ex2 = assertThrows(Exception.class, () -> invokeHandleLine("admin-modify-instructor 10"));
        assertTrue(ex2.getMessage().contains("Usage: admin-modify-instructor"));

        Exception ex3 = assertThrows(Exception.class, () -> invokeHandleLine("admin-remove-instructor"));
        assertTrue(ex3.getMessage().contains("Usage: admin-remove-instructor <staffId>"));
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
        invokeHandleLine("admin-modify-course --code CS211 --title Alias Update --credits 5");
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
        verify(administrativeService, atLeastOnce()).modifyCourse(modifyCaptor.capture());
        assertFalse(modifyCaptor.getAllValues().isEmpty());
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
                () -> invokeHandleLine("admin-create-course --code CS211 --title Object Oriented Programming --credits 3 --semester 2026A"));
        assertTrue(ex1.getMessage().contains("Unknown option(s) for admin-create-course: --semester"));

        Exception ex2 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-course --code CS211 --wrongField abc"));
        assertTrue(ex2.getMessage().contains("Unknown option(s) for admin-create-course: --wrongfield"));
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

    // ---------------------------
    // admin section commands
    // ---------------------------

    @Test
    void adminSectionCommands_shouldListCreateModifyAndRemoveSections() {
        setAdminSession("admin1");

        Course course = mock(Course.class);
        when(course.getCourseCode()).thenReturn("CS211");

        Instructor i1 = mockInstructor("ins1", "pwd", 7, "Inst One");
        Instructor i2 = mockInstructor("ins2", "pwd", 8, "Inst Two");

        Section listed = mock(Section.class);
        when(listed.getSectionId()).thenReturn(11);
        when(listed.getCourse()).thenReturn(course);
        when(listed.getType()).thenReturn(Section.Type.LECTURE);
        when(listed.getStartTime()).thenReturn(LocalDateTime.of(2025, 9, 1, 9, 0));
        when(listed.getEndTime()).thenReturn(LocalDateTime.of(2025, 9, 1, 10, 50));
        when(listed.getEnrollCapacity()).thenReturn(30);
        when(listed.getWaitlistCapacity()).thenReturn(10);
        when(listed.getVenue()).thenReturn("LT-1");
        when(listed.getInstructors()).thenReturn(Set.of(i1, i2));

        Section created = mock(Section.class);
        when(created.getSectionId()).thenReturn(12);

        Section updated = mock(Section.class);
        when(updated.getSectionId()).thenReturn(13);

        when(administrativeService.listSections(null)).thenReturn(List.of(listed));
        when(administrativeService.listSections("CS211")).thenReturn(List.of(listed));
        when(courseService.getCourse("CS211")).thenReturn(course);
        when(administrativeService.createSection(any(AdminSectionService.class))).thenReturn(created);
        when(administrativeService.modifySection(any(AdminSectionService.class))).thenReturn(updated);

        invokeHandleLine("admin-list-sections");
        invokeHandleLine("admin-list-sections --course CS211");
        invokeHandleLine("admin-create-section --course CS211 --type LECTURE --enroll-capacity 30 --waitlist-capacity 10 --start 2025-09-01T09:00 --end 2025-09-01T10:50 --venue LT-1 --instructors 7,8");
        invokeHandleLine("admin-modify-section --section-id 13 --course CS211 --type TUTORIAL --enroll-capacity 20 --waitlist-capacity 5 --start 2025-09-02T11:00 --end 2025-09-02T11:50 --venue Room-101 --instructors 7");
        invokeHandleLine("admin-remove-section 15");

        String out = output();
        assertTrue(out.contains("sectionId=11 | course=CS211 | type=LECTURE"));
        assertTrue(out.contains("Created section 12"));
        assertTrue(out.contains("Updated section 13"));
        assertTrue(out.contains("Removed section 15"));

        ArgumentCaptor<AdminSectionService> createCaptor = ArgumentCaptor.forClass(AdminSectionService.class);
        verify(administrativeService).createSection(createCaptor.capture());
        assertEquals(course, createCaptor.getValue().getCourse());
        assertEquals(Section.Type.LECTURE, createCaptor.getValue().getSectionType());
        assertEquals(30, createCaptor.getValue().getEnrollCapacity());
        assertEquals(10, createCaptor.getValue().getWaitlistCapacity());
        assertEquals(LocalDateTime.of(2025, 9, 1, 9, 0), createCaptor.getValue().getStartTime());
        assertEquals(LocalDateTime.of(2025, 9, 1, 10, 50), createCaptor.getValue().getEndTime());
        assertEquals("LT-1", createCaptor.getValue().getVenue());
        assertEquals(Set.of(7, 8), createCaptor.getValue().getInstructorStaffIds());

        ArgumentCaptor<AdminSectionService> modifyCaptor = ArgumentCaptor.forClass(AdminSectionService.class);
        verify(administrativeService).modifySection(modifyCaptor.capture());
        assertEquals(13, modifyCaptor.getValue().getSectionId());
        assertEquals(course, modifyCaptor.getValue().getCourse());
        assertEquals(Section.Type.TUTORIAL, modifyCaptor.getValue().getSectionType());
        assertEquals(20, modifyCaptor.getValue().getEnrollCapacity());
        assertEquals(5, modifyCaptor.getValue().getWaitlistCapacity());
        assertEquals(LocalDateTime.of(2025, 9, 2, 11, 0), modifyCaptor.getValue().getStartTime());
        assertEquals(LocalDateTime.of(2025, 9, 2, 11, 50), modifyCaptor.getValue().getEndTime());
        assertEquals("Room-101", modifyCaptor.getValue().getVenue());
        assertEquals(Set.of(7), modifyCaptor.getValue().getInstructorStaffIds());

        ArgumentCaptor<AdminSectionService> deleteCaptor = ArgumentCaptor.forClass(AdminSectionService.class);
        verify(administrativeService).deleteSection(deleteCaptor.capture());
        assertEquals(15, deleteCaptor.getValue().getSectionId());
    }

    @Test
    void adminSectionCommands_shouldCreateWithoutInstructorsAndModifyWithoutInstructors() {
        setAdminSession("admin1");

        Course course = mock(Course.class);
        when(course.getCourseCode()).thenReturn("CS300");
        when(courseService.getCourse("CS300")).thenReturn(course);

        Section created = mock(Section.class);
        when(created.getSectionId()).thenReturn(31);

        Section updated = mock(Section.class);
        when(updated.getSectionId()).thenReturn(32);

        when(administrativeService.createSection(any(AdminSectionService.class))).thenReturn(created);
        when(administrativeService.modifySection(any(AdminSectionService.class))).thenReturn(updated);

        invokeHandleLine("admin-create-section --course CS300 --type LAB --enroll-capacity 25 --waitlist-capacity 8 --start 2025-09-10T14:00 --end 2025-09-10T16:00 --venue Lab-A");
        invokeHandleLine("admin-modify-section --section-id 32 --course CS300 --venue Lab-B");

        ArgumentCaptor<AdminSectionService> createCaptor = ArgumentCaptor.forClass(AdminSectionService.class);
        verify(administrativeService).createSection(createCaptor.capture());
        assertNotNull(createCaptor.getValue().getInstructorStaffIds());
        assertTrue(createCaptor.getValue().getInstructorStaffIds().isEmpty());

        ArgumentCaptor<AdminSectionService> modifyCaptor = ArgumentCaptor.forClass(AdminSectionService.class);
        verify(administrativeService).modifySection(modifyCaptor.capture());
        assertNull(modifyCaptor.getValue().getInstructorStaffIds());
    }

    @Test
    void adminListSections_shouldPrintDashWhenFieldsMissing() {
        setAdminSession("admin1");

        Section listed = mock(Section.class);
        when(listed.getSectionId()).thenReturn(99);
        when(listed.getCourse()).thenReturn(null);
        when(listed.getType()).thenReturn(null);
        when(listed.getStartTime()).thenReturn(null);
        when(listed.getEndTime()).thenReturn(null);
        when(listed.getEnrollCapacity()).thenReturn(0);
        when(listed.getWaitlistCapacity()).thenReturn(0);
        when(listed.getVenue()).thenReturn(" ");
        when(listed.getInstructors()).thenReturn(Collections.emptySet());

        when(administrativeService.listSections(null)).thenReturn(List.of(listed));

        invokeHandleLine("admin-list-sections");

        String out = output();
        assertTrue(out.contains("sectionId=99 | course=- | type=- | start=- | end=- | enrollCap=0 | waitlistCap=0 | venue=- | instructors=-"));
    }

    @Test
    void adminSectionCommands_shouldValidateAndRejectUnknownOptions() {
        setAdminSession("admin1");

        Exception ex1 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-list-sections --bad value"));
        assertTrue(ex1.getMessage().contains("Unknown option(s) for admin-list-sections: --bad"));

        Exception ex2 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-section --type LECTURE --enroll-capacity 30 --waitlist-capacity 10 --start 2025-09-01T09:00 --end 2025-09-01T10:50 --venue LT-1"));
        assertTrue(ex2.getMessage().contains("--course is required"));

        Exception ex3 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-section --course CS211 --type BAD --enroll-capacity 30 --waitlist-capacity 10 --start 2025-09-01T09:00 --end 2025-09-01T10:50 --venue LT-1"));
        assertTrue(ex3.getMessage().contains("Course not found: CS211"));

        Course course = mock(Course.class);
        when(courseService.getCourse("CS211")).thenReturn(course);

        Exception ex4 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-section --course CS211 --type BAD --enroll-capacity 30 --waitlist-capacity 10 --start 2025-09-01T09:00 --end 2025-09-01T10:50 --venue LT-1"));
        assertTrue(ex4.getMessage().contains("Invalid section type"));

        Exception ex5 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-modify-section --course CS211"));
        assertTrue(ex5.getMessage().contains("--section-id is required"));

        Exception ex6 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-remove-section"));
        assertTrue(ex6.getMessage().contains("Usage: admin-remove-section <sectionId>"));

        Exception ex7 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-section --course CS211 --type LECTURE --enroll-capacity x --waitlist-capacity 10 --start 2025-09-01T09:00 --end 2025-09-01T10:50 --venue LT-1"));
        assertTrue(ex7.getMessage().contains("Invalid integer for enroll-capacity"));

        Exception ex8 = assertThrows(Exception.class,
                () -> invokeHandleLine("admin-create-section --course CS211 --type LECTURE --enroll-capacity 1 --waitlist-capacity 10 --start bad-date --end 2025-09-01T10:50 --venue LT-1"));
        assertTrue(ex8.getMessage().contains("Invalid date-time for start"));
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
    // unknown command / empty line
    // ---------------------------

    @Test
    void handleLine_shouldPrintUnknownCommand() {
        invokeHandleLine("something-unsupported");
        assertTrue(output().contains("Unknown command. Type help to see available commands."));
    }

    @Test
    void handleLine_shouldReturnWhenTokenListIsEmpty() {
        assertDoesNotThrow(() -> invokeHandleLine(""));
    }

    // ---------------------------
    // extra coverage for specific usage validations
    // ---------------------------

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
    void viewMasterSchedule_shouldPrintInstructorNA_whenNoInstructors() {
        setStudentSession("student1");
        Student student = mockStudent("student1", "pwd", 1001);
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

        Course course = mock(Course.class);
        when(course.getCourseCode()).thenReturn("CS100");
        when(course.getTitle()).thenReturn("Test");
        when(course.getCredits()).thenReturn(3);

        Section section = mock(Section.class);
        when(section.getSectionId()).thenReturn(1);
        when(section.getInstructors()).thenReturn(null);
        when(section.getEnrollCapacity()).thenReturn(10);
        when(section.getWaitlistCapacity()).thenReturn(5);

        when(course.getSections()).thenReturn(Set.of(section));
        when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));

        when(registrationRecordRepository.countEnrolled(1)).thenReturn(0);
        when(waitlistRecordRepository.countWaitlisted(1)).thenReturn(0);

        invokeHandleLine("view-master-schedule");

        assertTrue(output().contains("Instructors: N/A"));
    }

    @Test
void viewMasterSchedule_shouldPrintNAForNullDateTime() {
    setStudentSession("student1");
    Student student = mockStudent("student1", "pwd", 1001);
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Course course = mock(Course.class);
    when(course.getCourseCode()).thenReturn("CS200");
    when(course.getTitle()).thenReturn("No Time");
    when(course.getCredits()).thenReturn(3);

    Section section = mock(Section.class);
    when(section.getSectionId()).thenReturn(2);
    when(section.getStartTime()).thenReturn(null);
    when(section.getEndTime()).thenReturn(null);
    when(section.getEnrollCapacity()).thenReturn(10);
    when(section.getWaitlistCapacity()).thenReturn(5);
    when(section.getInstructors()).thenReturn(Collections.emptySet());

    when(course.getSections()).thenReturn(Set.of(section));
    when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));

    when(registrationRecordRepository.countEnrolled(2)).thenReturn(0);
    when(waitlistRecordRepository.countWaitlisted(2)).thenReturn(0);

    invokeHandleLine("view-master-schedule");

    assertTrue(output().contains("Time: N/A to N/A"));
}

@Test
void exportTimetable_shouldThrowForAdminRole() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine("export-timetable"));

    assertTrue(ex.getMessage().contains("Only Instrucotor and Student can export timetable"));
}

@Test
void addPlanEntry_shouldRejectInvalidArgumentCount() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine("add-plan-entry 1 2"));

    assertTrue(ex.getMessage().contains("Usage: add-plan-entry"));
}

@Test
void adminModifyStudent_shouldFailWhenPositionalTooShort() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine("admin-modify-student 1001"));

    assertTrue(ex.getMessage().contains("Usage: admin-modify-student"));
}

@Test
void adminModifyInstructor_shouldFailWhenPositionalTooShort() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine("admin-modify-instructor 200"));

    assertTrue(ex.getMessage().contains("Usage: admin-modify-instructor"));
}


@Test
void adminListSections_shouldPrintNoSectionsFound() {
    setAdminSession("admin1");
    when(administrativeService.listSections(null)).thenReturn(Collections.emptyList());

    invokeHandleLine("admin-list-sections");

    assertTrue(output().contains("No sections found."));
}

@Test
void adminListSections_shouldPrintDashForNullOrBlankInstructors() {
    setAdminSession("admin1");

    Section section = mock(Section.class);
    when(section.getSectionId()).thenReturn(99);
    when(section.getCourse()).thenReturn(null);
    when(section.getType()).thenReturn(null);
    when(section.getStartTime()).thenReturn(null);
    when(section.getEndTime()).thenReturn(null);
    when(section.getEnrollCapacity()).thenReturn(0);
    when(section.getWaitlistCapacity()).thenReturn(0);
    when(section.getVenue()).thenReturn(" ");
    when(section.getInstructors()).thenReturn(Collections.emptySet());

    when(administrativeService.listSections(null)).thenReturn(List.of(section));

    invokeHandleLine("admin-list-sections");

    assertTrue(output().contains("instructors=-"));
}


@Test
void adminCreateSection_shouldRejectUnknownOptions() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine("admin-create-section --course CS100 --bad value"));

    assertTrue(ex.getMessage().contains("Unknown option(s)"));
}

@Test
void parseIntegerCsv_shouldHandleNullBlankAndEmptyValues() {
    setAdminSession("admin1");

    Course course = mock(Course.class);
    when(courseService.getCourse("CS100")).thenReturn(course);

    invokeHandleLine(
        "admin-create-section --course CS100 --type LECTURE " +
        "--enroll-capacity 10 --waitlist-capacity 5 " +
        "--start 2025-01-01T09:00 --end 2025-01-01T10:00 " +
        "--venue LT1 --instructors \" , , \""
    );

    ArgumentCaptor<AdminSectionService> captor =
        ArgumentCaptor.forClass(AdminSectionService.class);

    verify(administrativeService).createSection(captor.capture());
    assertTrue(captor.getValue().getInstructorStaffIds().isEmpty());
}

@Test
void requireInstructor_shouldFailForStudentRole() {
    setStudentSession("student1");

    Exception ex = assertThrows(Exception.class,
        () -> ReflectionTestUtils.invokeMethod(runner, "requireInstructor"));

    assertTrue(ex.getMessage().contains("INSTRUCTOR role"));
}

@Test
void exportTimetable_shouldUseCustomPathForInstructor() throws Exception {
    Instructor instructor = mockInstructor("ins1", "pwd", 2001, "Instructor One");
    setInstructorSession("ins1");
    when(instructorRepository.findByUserEID("ins1")).thenReturn(Optional.of(instructor));

    Path generated = tempDir.resolve("gen.txt");
    Files.writeString(generated, "data");
    when(timetableService.exportInstructorTimetable(2001)).thenReturn(generated);

    Path custom = tempDir.resolve("custom.txt");
    invokeHandleLine("export-timetable \"" + custom + "\"");

    assertTrue(Files.exists(custom));
    assertFalse(Files.exists(generated));
    assertTrue(output().contains(custom.toAbsolutePath().toString()));
}

@Test
void listPlans_shouldContinueWhenEntriesIsNull() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    RegistrationPlan plan = mock(RegistrationPlan.class);
    when(plan.getPlanId()).thenReturn(10);
    when(plan.getPriority()).thenReturn(1);
    when(plan.getApplyStatus()).thenReturn(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
    when(plan.getApplySummary()).thenReturn("test");
    when(plan.getEntries()).thenReturn(null);

    when(registrationPlanService.getPlanSet(1001)).thenReturn(List.of(plan));

    invokeHandleLine("list-plans");

    assertTrue(output().contains("planId=10 | priority=1"));
}

@Test
void adminModifyStudent_shouldFailWhenMissingUserEidAndName() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine("admin-modify-student 100"));

    assertTrue(ex.getMessage().contains("Usage: admin-modify-student"));
}

@Test
void adminModifyInstructor_shouldFailWhenMissingUserEidAndName() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine("admin-modify-instructor 200"));

    assertTrue(ex.getMessage().contains("Usage: admin-modify-instructor"));
}

@Test
void adminListSections_shouldHandleNullInstructors() {
    setAdminSession("admin1");

    Section section = mock(Section.class);
    when(section.getSectionId()).thenReturn(1);
    when(section.getCourse()).thenReturn(null);
    when(section.getType()).thenReturn(null);
    when(section.getStartTime()).thenReturn(null);
    when(section.getEndTime()).thenReturn(null);
    when(section.getEnrollCapacity()).thenReturn(0);
    when(section.getWaitlistCapacity()).thenReturn(0);
    when(section.getVenue()).thenReturn(" ");
    when(section.getInstructors()).thenReturn(null);

    when(administrativeService.listSections(null)).thenReturn(List.of(section));

    invokeHandleLine("admin-list-sections");

    assertTrue(output().contains("instructors=-"));
}
@Test
void adminCreateSection_shouldFailForEachMissingRequiredField() {
    setAdminSession("admin1");

    assertThrows(Exception.class,
        () -> invokeHandleLine("admin-create-section"));

    assertThrows(Exception.class,
        () -> invokeHandleLine("admin-create-section --course CS1"));

    assertThrows(Exception.class,
        () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE"));

    assertThrows(Exception.class,
        () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10"));

    assertThrows(Exception.class,
        () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5"));

    assertThrows(Exception.class,
        () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5 --start 2025-01-01T09:00"));

    assertThrows(Exception.class,
        () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5 --start 2025-01-01T09:00 --end 2025-01-01T10:00"));
}

@Test
void parseIntegerCsv_shouldReturnEmptySetForBlankCsv() {
    setAdminSession("admin1");

    Course course = mock(Course.class);
    when(courseService.getCourse("CS1")).thenReturn(course);

    invokeHandleLine(
        "admin-create-section --course CS1 --type LECTURE " +
        "--enroll-capacity 10 --waitlist-capacity 5 " +
        "--start 2025-01-01T09:00 --end 2025-01-01T10:00 " +
        "--venue LT1 --instructors \" \""
    );

    ArgumentCaptor<AdminSectionService> captor =
        ArgumentCaptor.forClass(AdminSectionService.class);

    verify(administrativeService).createSection(captor.capture());
    assertTrue(captor.getValue().getInstructorStaffIds().isEmpty());
}

@Test
void adminModifyStudent_shouldHitPositionalSizeLessThan2Branch() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine(
            "admin-modify-student 1001 --major CS"
        )
    );

    assertTrue(ex.getMessage().contains("Usage: admin-modify-student"));
}

@Test
void adminModifyInstructor_shouldHitPositionalSizeLessThan2Branch() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
        () -> invokeHandleLine(
            "admin-modify-instructor 200 --dept CS"
        )
    );

    assertTrue(ex.getMessage().contains("Usage: admin-modify-instructor"));
}


@Test
void exportTimetable_shouldUseCustomPathForInstructor1() throws Exception {
    Instructor instructor = mockInstructor("ins1", "pwd", 2001, "Instructor One");
    setInstructorSession("ins1");
    when(instructorRepository.findByUserEID("ins1")).thenReturn(Optional.of(instructor));

    Path generated = tempDir.resolve("gen.txt");
    Files.writeString(generated, "data");
    when(timetableService.exportInstructorTimetable(2001)).thenReturn(generated);

    Path custom = tempDir.resolve("custom.txt");
    invokeHandleLine("export-timetable \"" + custom + "\"");

    assertTrue(Files.exists(custom));
    assertFalse(Files.exists(generated));
    assertTrue(output().contains(custom.toAbsolutePath().toString()));
}

@Test
void listPlans_shouldContinueWhenEntriesIsNull1() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    RegistrationPlan plan = mock(RegistrationPlan.class);
    when(plan.getPlanId()).thenReturn(10);
    when(plan.getPriority()).thenReturn(1);
    when(plan.getApplyStatus()).thenReturn(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
    when(plan.getApplySummary()).thenReturn("test");
    when(plan.getEntries()).thenReturn(null);

    when(registrationPlanService.getPlanSet(1001)).thenReturn(List.of(plan));

    invokeHandleLine("list-plans");

    assertTrue(output().contains("planId=10 | priority=1"));
}

@Test
void addPlanEntry_shouldRejectInvalidArgumentCount1() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Exception ex = assertThrows(Exception.class,
            () -> invokeHandleLine("add-plan-entry 1 2"));

    assertTrue(ex.getMessage().contains("Usage: add-plan-entry"));
}

@Test
void addPlanEntry_shouldCoverFalseBooleanBranches() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    PlanEntry entry1 = new PlanEntry();
    entry1.setEntryId(1);

    PlanEntry entry2 = new PlanEntry();
    entry2.setEntryId(2);

    when(registrationPlanService.addEntry(11, 88, PlanEntry.EntryType.SELECTED, false)).thenReturn(entry1);
    when(registrationPlanService.addEntry(12, 89, PlanEntry.EntryType.WAITLIST, false)).thenReturn(entry2);

    invokeHandleLine("add-plan-entry 11 88 selected false");
    invokeHandleLine("add-plan-entry 12 89 waitlist");

    verify(registrationPlanService).addEntry(11, 88, PlanEntry.EntryType.SELECTED, false);
    verify(registrationPlanService).addEntry(12, 89, PlanEntry.EntryType.WAITLIST, false);
}

@Test
void listPlans_shouldHandleEmptyEntriesListAndNullSection() {
    Student student = mockStudent("student1", "pwd", 1001);
    setStudentSession("student1");
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    RegistrationPlan emptyPlan = new RegistrationPlan();
    emptyPlan.setPlanId(1);
    emptyPlan.setPriority(1);
    emptyPlan.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
    emptyPlan.setApplySummary("empty");

    RegistrationPlan nullSectionPlan = new RegistrationPlan();
    nullSectionPlan.setPlanId(2);
    nullSectionPlan.setPriority(2);
    nullSectionPlan.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
    nullSectionPlan.setApplySummary("null section");

    PlanEntry entry = new PlanEntry();
    entry.setEntryId(99);
    entry.setSection(null);
    entry.setEntryType(PlanEntry.EntryType.WAITLIST);
    entry.setStatus(PlanEntry.EntryStatus.PENDING);
    nullSectionPlan.addEntry(entry);

    when(registrationPlanService.getPlanSet(1001)).thenReturn(List.of(emptyPlan, nullSectionPlan));

    invokeHandleLine("list-plans");

    String out = output();
    assertTrue(out.contains("planId=1 | priority=1"));
    assertTrue(out.contains("planId=2 | priority=2"));
    assertTrue(out.contains("sectionId=-"));
}

@Test
void adminModifyStudent_shouldHitPositionalSizeLessThan2Branch1() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-modify-student 1001 --major CS"));

    assertTrue(ex.getMessage().contains("Usage: admin-modify-student"));
}

@Test
void adminModifyInstructor_shouldHitPositionalSizeLessThan2Branch1() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-modify-instructor 200 --dept CS"));

    assertTrue(ex.getMessage().contains("Usage: admin-modify-instructor"));
}

@Test
void adminCreateCourse_shouldCoverSplitCsvBlankBranch() {
    setAdminSession("admin1");

    Course created = mockCourse("CS300", "Title", 3, 0);
    when(courseService.getCourse("CS300")).thenReturn(null);
    when(administrativeService.createCourse(any(AdminCourseRequest.class))).thenReturn(created);

    invokeHandleLine("admin-create-course --code CS300 --title T --credits 3 --prereq \" \" --exclusive \" \"");

    ArgumentCaptor<AdminCourseRequest> captor = ArgumentCaptor.forClass(AdminCourseRequest.class);
    verify(administrativeService).createCourse(captor.capture());

    assertNull(captor.getValue().getPrerequisiteCourseCodes());
    assertNull(captor.getValue().getExclusiveCourseCodes());
}

@Test
void adminCreateCourse_shouldRejectBlankTitleAndBlankCredits() {
    setAdminSession("admin1");

    when(courseService.getCourse("CS301")).thenReturn(null);
    when(courseService.getCourse("CS302")).thenReturn(null);

    Exception ex1 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-course --code CS301 --title \" \" --credits 3"));
    assertTrue(ex1.getMessage().contains("--title is required when creating a new course"));

    Exception ex2 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-course --code CS302 --title Valid --credits \" \""));
    assertTrue(ex2.getMessage().contains("--credits is required when creating a new course"));
}

@Test
void adminCreateCourse_shouldRejectBlankCode() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-course --code \" \" --title T --credits 3"));

    assertTrue(ex.getMessage().contains("--code is required"));
}

@Test
void adminCreatePeriod_shouldRejectBlankCohortStartAndEnd() {
    setAdminSession("admin1");

    Exception ex1 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-period --cohort \" \" --start 2025-01-01T09:00 --end 2025-01-02T09:00"));
    assertTrue(ex1.getMessage().contains("Usage: admin-create-period"));

    Exception ex2 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-period --cohort 2024 --start \" \" --end 2025-01-02T09:00"));
    assertTrue(ex2.getMessage().contains("Usage: admin-create-period"));

    Exception ex3 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-period --cohort 2024 --start 2025-01-01T09:00 --end \" \""));
    assertTrue(ex3.getMessage().contains("Usage: admin-create-period"));
}

@Test
void adminListPeriods_shouldIgnoreUnknownOptionAndLeaveCohortNull() {
    setAdminSession("admin1");
    when(administrativeService.listRegistrationPeriods(null)).thenReturn(Collections.emptyList());

    invokeHandleLine("admin-list-periods --something else");

    verify(administrativeService).listRegistrationPeriods(null);
    assertTrue(output().contains("No registration periods found."));
}

@Test
void adminCreateSection_shouldFailForEachMissingRequiredField1() {
    setAdminSession("admin1");

    assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section"));

    assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1"));

    assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE"));

    assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10"));

    assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5"));

    assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5 --start 2025-01-01T09:00"));

    assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5 --start 2025-01-01T09:00 --end 2025-01-01T10:00"));
}

@Test
void adminCreateSection_shouldRejectBlankRequiredFields() {
    setAdminSession("admin1");

    Exception ex1 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course \" \" --type LECTURE --enroll-capacity 10 --waitlist-capacity 5 --start 2025-01-01T09:00 --end 2025-01-01T10:00 --venue LT1"));
    assertTrue(ex1.getMessage().contains("--course is required"));

    Exception ex2 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type \" \" --enroll-capacity 10 --waitlist-capacity 5 --start 2025-01-01T09:00 --end 2025-01-01T10:00 --venue LT1"));
    assertTrue(ex2.getMessage().contains("--type is required"));

    Exception ex3 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity \" \" --waitlist-capacity 5 --start 2025-01-01T09:00 --end 2025-01-01T10:00 --venue LT1"));
    assertTrue(ex3.getMessage().contains("--enroll-capacity is required"));

    Exception ex4 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity \" \" --start 2025-01-01T09:00 --end 2025-01-01T10:00 --venue LT1"));
    assertTrue(ex4.getMessage().contains("--waitlist-capacity is required"));

    Exception ex5 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5 --start \" \" --end 2025-01-01T10:00 --venue LT1"));
    assertTrue(ex5.getMessage().contains("--start is required"));

    Exception ex6 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5 --start 2025-01-01T09:00 --end \" \" --venue LT1"));
    assertTrue(ex6.getMessage().contains("--end is required"));

    Exception ex7 = assertThrows(Exception.class,
            () -> invokeHandleLine("admin-create-section --course CS1 --type LECTURE --enroll-capacity 10 --waitlist-capacity 5 --start 2025-01-01T09:00 --end 2025-01-01T10:00 --venue \" \""));
    assertTrue(ex7.getMessage().contains("--venue is required"));
}

@Test
void adminModifySection_shouldCoverBlankOptionalFieldBranches() {
    setAdminSession("admin1");

    Section updated = mock(Section.class);
    when(updated.getSectionId()).thenReturn(55);
    when(administrativeService.modifySection(any(AdminSectionService.class))).thenReturn(updated);

    invokeHandleLine(
            "admin-modify-section --section-id 55 " +
            "--course \" \" --type \" \" --enroll-capacity \" \" --waitlist-capacity \" \" " +
            "--start \" \" --end \" \" --venue \" \" --instructors \" \""
    );

    ArgumentCaptor<AdminSectionService> captor = ArgumentCaptor.forClass(AdminSectionService.class);
    verify(administrativeService).modifySection(captor.capture());

    AdminSectionService req = captor.getValue();
    assertEquals(55, req.getSectionId());
    assertNull(req.getCourse());
    assertNull(req.getSectionType());
    assertNull(req.getEnrollCapacity());
    assertNull(req.getWaitlistCapacity());
    assertNull(req.getStartTime());
    assertNull(req.getEndTime());
    assertEquals(" ", req.getVenue());
    assertNotNull(req.getInstructorStaffIds());
    assertTrue(req.getInstructorStaffIds().isEmpty());
}

@Test
void adminCreateSection_shouldCoverParseIntegerCsvBlankBranch() {
    setAdminSession("admin1");

    Course course = mock(Course.class);
    when(courseService.getCourse("CS1")).thenReturn(course);

    Section created = mock(Section.class);
    when(created.getSectionId()).thenReturn(88);
    when(administrativeService.createSection(any(AdminSectionService.class))).thenReturn(created);

    invokeHandleLine(
            "admin-create-section --course CS1 --type LECTURE " +
            "--enroll-capacity 10 --waitlist-capacity 5 " +
            "--start 2025-01-01T09:00 --end 2025-01-01T10:00 " +
            "--venue LT1 --instructors \" \""
    );

    ArgumentCaptor<AdminSectionService> captor = ArgumentCaptor.forClass(AdminSectionService.class);
    verify(administrativeService).createSection(captor.capture());

    assertNotNull(captor.getValue().getInstructorStaffIds());
    assertTrue(captor.getValue().getInstructorStaffIds().isEmpty());
}

@Test
void adminListSections_shouldHandleNullInstructors1() {
    setAdminSession("admin1");

    Section section = mock(Section.class);
    when(section.getSectionId()).thenReturn(1);
    when(section.getCourse()).thenReturn(null);
    when(section.getType()).thenReturn(null);
    when(section.getStartTime()).thenReturn(null);
    when(section.getEndTime()).thenReturn(null);
    when(section.getEnrollCapacity()).thenReturn(0);
    when(section.getWaitlistCapacity()).thenReturn(0);
    when(section.getVenue()).thenReturn(" ");
    when(section.getInstructors()).thenReturn(null);

    when(administrativeService.listSections(null)).thenReturn(List.of(section));

    invokeHandleLine("admin-list-sections");

    assertTrue(output().contains("instructors=-"));
}

@Test
void adminListSections_shouldPrintNoSectionsFound1() {
    setAdminSession("admin1");
    when(administrativeService.listSections(null)).thenReturn(Collections.emptyList());

    invokeHandleLine("admin-list-sections");

    assertTrue(output().contains("No sections found."));
}

@Test
void adminListSections_shouldPrintDashWhenVenueIsNull() {
    setAdminSession("admin1");

    Section section = mock(Section.class);
    when(section.getSectionId()).thenReturn(99);
    when(section.getCourse()).thenReturn(null);
    when(section.getType()).thenReturn(null);
    when(section.getStartTime()).thenReturn(null);
    when(section.getEndTime()).thenReturn(null);
    when(section.getEnrollCapacity()).thenReturn(0);
    when(section.getWaitlistCapacity()).thenReturn(0);
    when(section.getVenue()).thenReturn(null);
    when(section.getInstructors()).thenReturn(Collections.emptySet());

    when(administrativeService.listSections(null)).thenReturn(List.of(section));

    invokeHandleLine("admin-list-sections");

    assertTrue(output().contains("venue=-"));
}

@Test
void viewMasterSchedule_shouldPrintInstructorNA_whenNoInstructors1() {
    setStudentSession("student1");
    Student student = mockStudent("student1", "pwd", 1001);
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Course course = mock(Course.class);
    when(course.getCourseCode()).thenReturn("CS100");
    when(course.getTitle()).thenReturn("Test");
    when(course.getCredits()).thenReturn(3);

    Section section = mock(Section.class);
    when(section.getSectionId()).thenReturn(1);
    when(section.getInstructors()).thenReturn(null);
    when(section.getEnrollCapacity()).thenReturn(10);
    when(section.getWaitlistCapacity()).thenReturn(5);

    when(course.getSections()).thenReturn(Set.of(section));
    when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));

    when(registrationRecordRepository.countEnrolled(1)).thenReturn(0);
    when(waitlistRecordRepository.countWaitlisted(1)).thenReturn(0);

    invokeHandleLine("view-master-schedule");

    assertTrue(output().contains("Instructors: N/A"));
}

@Test
void viewMasterSchedule_shouldPrintNAForNullDateTime1() {
    setStudentSession("student1");
    Student student = mockStudent("student1", "pwd", 1001);
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Course course = mock(Course.class);
    when(course.getCourseCode()).thenReturn("CS200");
    when(course.getTitle()).thenReturn("No Time");
    when(course.getCredits()).thenReturn(3);

    Section section = mock(Section.class);
    when(section.getSectionId()).thenReturn(2);
    when(section.getStartTime()).thenReturn(null);
    when(section.getEndTime()).thenReturn(null);
    when(section.getEnrollCapacity()).thenReturn(10);
    when(section.getWaitlistCapacity()).thenReturn(5);
    when(section.getInstructors()).thenReturn(Collections.emptySet());

    when(course.getSections()).thenReturn(Set.of(section));
    when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));

    when(registrationRecordRepository.countEnrolled(2)).thenReturn(0);
    when(waitlistRecordRepository.countWaitlisted(2)).thenReturn(0);

    invokeHandleLine("view-master-schedule");

    assertTrue(output().contains("Time: N/A to N/A"));
}

@Test
void viewMasterSchedule_shouldHandleNullSections() {
    setStudentSession("student1");
    Student student = mockStudent("student1", "pwd", 1001);
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Course course = mock(Course.class);
    when(course.getCourseCode()).thenReturn("CS400");
    when(course.getTitle()).thenReturn("Null Sections");
    when(course.getCredits()).thenReturn(3);
    when(course.getSections()).thenReturn(null);
    when(course.getPrerequisiteCourses()).thenReturn(null);
    when(course.getExclusiveCourses()).thenReturn(null);

    when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));

    invokeHandleLine("view-master-schedule");

    assertTrue(output().contains("[No sections available]"));
}

@Test
void viewMasterSchedule_shouldHandleEmptyPrereqAndExclusiveSets() {
    setStudentSession("student1");
    Student student = mockStudent("student1", "pwd", 1001);
    when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(student));

    Course course = mock(Course.class);
    when(course.getCourseCode()).thenReturn("CS401");
    when(course.getTitle()).thenReturn("Empty Relations");
    when(course.getCredits()).thenReturn(3);
    when(course.getPrerequisiteCourses()).thenReturn(Collections.emptySet());
    when(course.getExclusiveCourses()).thenReturn(Collections.emptySet());
    when(course.getSections()).thenReturn(Collections.emptySet());

    when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));

    invokeHandleLine("view-master-schedule");

    String out = output();
    assertFalse(out.contains("Prerequisites:"));
    assertFalse(out.contains("Exclusive Courses:"));
}

@Test
void exportTimetable_shouldThrowForAdminRole1() {
    setAdminSession("admin1");

    Exception ex = assertThrows(Exception.class,
            () -> invokeHandleLine("export-timetable"));

    assertTrue(ex.getMessage().contains("Only Instrucotor and Student can export timetable"));
}

@Test
void requireInstructor_shouldFailForStudentRole1() {
    setStudentSession("student1");

    Exception ex = assertThrows(Exception.class,
            () -> ReflectionTestUtils.invokeMethod(runner, "requireInstructor"));

    assertTrue(ex.getMessage().contains("INSTRUCTOR role"));
}


}



