package org.cityuhk.CourseRegistrationSystem.Cli;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import org.cityuhk.CourseRegistrationSystem.Model.*;
import org.cityuhk.CourseRegistrationSystem.Repository.*;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.*;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationPlanService;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

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
    private InteractiveCliRunner cliRunner;

    private Admin testAdmin;
    private Student testStudent;
    private Instructor testInstructor;
    private Course testCourse;
    private Section testSection;

    @BeforeEach
    void setUp() {
        testAdmin = mock(Admin.class);
        testStudent = mock(Student.class);
        testInstructor = mock(Instructor.class);
        testCourse = mock(Course.class);
        testSection = mock(Section.class);
    }

    // ============== Authentication Tests ==============

    @Test
    void testLoginInvalidCredentials() throws Exception {
        when(adminRepository.findByUserEID("invalid")).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID("invalid")).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID("invalid")).thenReturn(Optional.empty());

        List<String> args = Arrays.asList("invalid", "password");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleLogin", List.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testLoginInvalidArgs() throws Exception {
        List<String> args = List.of("only_one_arg");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleLogin", List.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testLoginAdminSuccess() throws Exception {
        when(testAdmin.getPassword()).thenReturn("hashed-admin-password");
        when(testAdmin.getUserEID()).thenReturn("admin1");
        when(adminRepository.findByUserEID("admin1")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("password", "hashed-admin-password")).thenReturn(true);

        List<String> args = Arrays.asList("admin1", "password");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleLogin", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(sessionStore).save(argThat(session ->
                "admin1".equals(session.getUserEid()) && session.getRole() == CliRole.ADMIN));
    }

    @Test
    void testLoginStudentSuccess() throws Exception {
        when(testStudent.getPassword()).thenReturn("hashed-student-password");
        when(testStudent.getUserEID()).thenReturn("student1");
        when(adminRepository.findByUserEID("student1")).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.matches("password", "hashed-student-password")).thenReturn(true);

        List<String> args = Arrays.asList("student1", "password");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleLogin", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(sessionStore).save(argThat(session ->
                "student1".equals(session.getUserEid()) && session.getRole() == CliRole.STUDENT));
    }

    @Test
    void testLoginInstructorSuccessWithPlaintextFallback() throws Exception {
        when(testInstructor.getPassword()).thenReturn("password");
        when(testInstructor.getUserEID()).thenReturn("instructor1");
        when(adminRepository.findByUserEID("instructor1")).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID("instructor1")).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID("instructor1")).thenReturn(Optional.of(testInstructor));
        when(passwordEncoder.matches("password", "password")).thenThrow(new IllegalArgumentException());

        List<String> args = Arrays.asList("instructor1", "password");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleLogin", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(sessionStore).save(argThat(session ->
                "instructor1".equals(session.getUserEid()) && session.getRole() == CliRole.INSTRUCTOR));
    }

    @Test
    void testPasswordMatchesWithNull() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("passwordMatches", String.class, String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(cliRunner, null, "password");
        assertFalse(result);

        result = (boolean) method.invoke(cliRunner, "password", null);
        assertFalse(result);
    }

    @Test
    void testPasswordMatchesWithEncoder() throws Exception {
        when(passwordEncoder.matches(eq("password"), any())).thenReturn(true);
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("passwordMatches", String.class, String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(cliRunner, "password", "hashedPassword");
        assertTrue(result);
    }

    @Test
    void testPasswordMatchesPlaintext() throws Exception {
        when(passwordEncoder.matches(anyString(), anyString())).thenThrow(new IllegalArgumentException());
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("passwordMatches", String.class, String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(cliRunner, "password", "password");
        assertTrue(result);
    }

    @Test
    void testLogout() throws Exception {
        setActiveStudentSession();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleLogout");
        method.setAccessible(true);
        method.invoke(cliRunner);
        verify(sessionStore).clear();
    }

    @Test
    void testWhoAmI() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleWhoAmI");
        method.setAccessible(true);
        method.invoke(cliRunner);
    }

    // ============== Student Command Tests ==============

    @Test
    void testAddSection() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = List.of("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAddSection", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationService).addSection(eq(0), eq(1), any(LocalDateTime.class));
    }

    @Test
    void testAddSectionInvalidArgs() throws Exception {
        setActiveStudentSession();
        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAddSection", List.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testDropSection() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = List.of("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleDropSection", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationService).dropSection(eq(0), eq(1), any(LocalDateTime.class));
    }

    @Test
    void testJoinWaitlist() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = List.of("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleJoinWaitlist", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationService).waitListSection(eq(0), eq(1), any(LocalDateTime.class));
    }

    @Test
    void testDropWaitlist() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = List.of("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleDropWaitlist", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationService).dropWaitlist(eq(0), eq(1));
    }

    // ============== Registration Plan Tests ==============

    @Test
    void testListPlans() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        RegistrationPlan plan = mock(RegistrationPlan.class);
        when(plan.getPlanId()).thenReturn(1);
        when(plan.getPriority()).thenReturn(1);
        when(plan.getApplyStatus()).thenReturn(RegistrationPlan.ApplyStatus.APPLIED);
        when(plan.getApplySummary()).thenReturn("Test Summary");
        when(plan.getEntries()).thenReturn(new ArrayList<>());

        when(registrationPlanService.getPlanSet(0)).thenReturn(List.of(plan));

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleListPlans");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(registrationPlanService).getPlanSet(0);
    }

    @Test
    void testListPlansEmpty() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));
        when(registrationPlanService.getPlanSet(0)).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleListPlans");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(registrationPlanService).getPlanSet(0);
    }

    @Test
    void testCreatePlanWithPriority() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        RegistrationPlan plan = mock(RegistrationPlan.class);
        when(plan.getPlanId()).thenReturn(1);
        when(plan.getPriority()).thenReturn(2);

        when(registrationPlanService.createPlan(0, 2)).thenReturn(plan);

        List<String> args = List.of("2");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleCreatePlan", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationPlanService).createPlan(0, 2);
    }

    @Test
    void testCreatePlanWithoutPriority() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        RegistrationPlan plan = mock(RegistrationPlan.class);
        when(plan.getPlanId()).thenReturn(1);
        when(plan.getPriority()).thenReturn(1);

        when(registrationPlanService.createPlan(0, null)).thenReturn(plan);

        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleCreatePlan", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationPlanService).createPlan(0, null);
    }

    @Test
    void testRemovePlan() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = List.of("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleRemovePlan", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationPlanService).removePlan(1);
    }

    @Test
    void testAddPlanEntry() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        PlanEntry entry = mock(PlanEntry.class);
        when(entry.getEntryId()).thenReturn(1);

        when(registrationPlanService.addEntry(1, 1, PlanEntry.EntryType.SELECTED, false))
                .thenReturn(entry);

        List<String> args = Arrays.asList("1", "1", "SELECTED", "false");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAddPlanEntry", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationPlanService).addEntry(1, 1, PlanEntry.EntryType.SELECTED, false);
    }

    @Test
    void testAddPlanEntryWithoutFlag() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        PlanEntry entry = mock(PlanEntry.class);
        when(entry.getEntryId()).thenReturn(1);

        when(registrationPlanService.addEntry(1, 1, PlanEntry.EntryType.WAITLIST, false))
                .thenReturn(entry);

        List<String> args = Arrays.asList("1", "1", "WAITLIST");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAddPlanEntry", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationPlanService).addEntry(1, 1, PlanEntry.EntryType.WAITLIST, false);
    }

    @Test
    void testAddPlanEntryInvalidType() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = Arrays.asList("1", "1", "INVALID");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAddPlanEntry", List.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testRemovePlanEntry() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = Arrays.asList("1", "1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleRemovePlanEntry", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationPlanService).removeEntry(1, 1);
    }

    @Test
    void testReorderPlans() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        RegistrationPlan plan1 = mock(RegistrationPlan.class);
        when(plan1.getPlanId()).thenReturn(1);
        when(plan1.getPriority()).thenReturn(1);

        when(registrationPlanService.reorderPlans(0, Arrays.asList(1, 2)))
                .thenReturn(List.of(plan1));

        List<String> args = List.of("1,2");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleReorderPlans", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationPlanService).reorderPlans(0, Arrays.asList(1, 2));
    }

    // ============== Timetable Tests ==============

    @Test
    void testShowTimetableStudent() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));
        when(timetableService.getStudentTimetableString(0)).thenReturn("Timetable content");

        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleShowTimeTable", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(timetableService).getStudentTimetableString(0);
    }

    @Test
    void testShowTimetableInstructor() throws Exception {
        setActiveInstructorSession();
        when(instructorRepository.findByUserEID("instructor1")).thenReturn(Optional.of(testInstructor));
        when(timetableService.getStudentTimetableString(0)).thenReturn("Instructor timetable");

        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleShowTimeTable", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(timetableService).getStudentTimetableString(0);
    }

    @Test
    void testExportTimetableStudentDefault() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        Path tempFile = Files.createTempFile("timetable", ".txt");
        when(timetableService.exportStudentTimetable(0)).thenReturn(tempFile);

        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleExportTimetable", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(timetableService).exportStudentTimetable(0);
        Files.deleteIfExists(tempFile);
    }



    @Test
    void testExportTimetableAdminNotAllowed() throws Exception {
        setActiveAdminSession();
        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleExportTimetable", List.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    // ============== Admin User Management Tests ==============

    @Test
    void testAdminListUsers() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listUsers()).thenReturn(Collections.singletonList(testAdmin));

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListUsers");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(administrativeService).listUsers();
    }

    @Test
    void testAdminListUsersEmpty() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listUsers()).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListUsers");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(administrativeService).listUsers();
    }

    @Test
    void testAdminCreateUser() throws Exception {
        setActiveAdminSession();
        when(administrativeService.createUser(any())).thenReturn(testAdmin);

        List<String> args = Arrays.asList("admin1", "Admin User", "password");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreateUser", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).createUser(any(AdminUserRequest.class));
    }

    @Test
    void testAdminModifyUser() throws Exception {
        setActiveAdminSession();
        when(administrativeService.modifyUser(any())).thenReturn(testAdmin);

        List<String> args = Arrays.asList("admin1", "--name", "New Name", "--password", "newpass");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminModifyUser", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).modifyUser(any(AdminUserRequest.class));
    }

    @Test
    void testAdminRemoveUser() throws Exception {
        setActiveAdminSession();
        List<String> args = List.of("admin1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminRemoveUser", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).removeUser("admin1");
    }

    // ============== Admin Student Management Tests ==============

    @Test
    void testAdminListStudents() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listStudents()).thenReturn(Arrays.asList(testStudent));

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListStudents");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(administrativeService).listStudents();
    }

    @Test
    void testAdminListStudentsEmpty() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listStudents()).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListStudents");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(administrativeService).listStudents();
    }

    @Test
    void testAdminCreateStudent() throws Exception {
        setActiveAdminSession();
        when(administrativeService.createStudent(any())).thenReturn(testStudent);

        List<String> args = Arrays.asList("student1", "Student User", "password", "12", "18", "CS", "2024", "CS", "120");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreateStudent", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).createStudent(any(AdminStudentRequest.class));
    }

    @Test
    void testAdminModifyStudent() throws Exception {
        setActiveAdminSession();
        when(administrativeService.modifyStudent(any())).thenReturn(testStudent);

        List<String> args = Arrays.asList("student1", "--name", "New Name", "--min-creds", "12");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminModifyStudent", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).modifyStudent(any(AdminStudentRequest.class));
    }

    @Test
    void testAdminRemoveStudent() throws Exception {
        setActiveAdminSession();
        List<String> args = List.of("student1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminRemoveStudent", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).removeStudent("student1");
    }

    // ============== Admin Instructor Management Tests ==============

    @Test
    void testAdminListInstructors() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listInstructors()).thenReturn(Collections.singletonList(testInstructor));

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListInstructors");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(administrativeService).listInstructors();
    }

    @Test
    void testAdminListInstructorsEmpty() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listInstructors()).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListInstructors");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(administrativeService).listInstructors();
    }

    @Test
    void testAdminCreateInstructor() throws Exception {
        setActiveAdminSession();
        when(administrativeService.createInstructor(any())).thenReturn(testInstructor);

        List<String> args = Arrays.asList("instructor1", "Instructor User", "password", "--dept", "CS");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreateInstructor", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).createInstructor(any(AdminInstructorRequest.class));
    }

    @Test
    void testAdminCreateInstructorMinimumArgs() throws Exception {
        setActiveAdminSession();
        when(administrativeService.createInstructor(any())).thenReturn(testInstructor);

        List<String> args = Arrays.asList("instructor1", "Instructor User", "password");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreateInstructor", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).createInstructor(any(AdminInstructorRequest.class));
    }

    @Test
    void testAdminModifyInstructor() throws Exception {
        setActiveAdminSession();
        when(administrativeService.modifyInstructor(any())).thenReturn(testInstructor);

        List<String> args = Arrays.asList("instructor1", "--name", "New Name");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminModifyInstructor", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).modifyInstructor(any(AdminInstructorRequest.class));
    }

    @Test
    void testAdminRemoveInstructor() throws Exception {
        setActiveAdminSession();
        List<String> args = List.of("instructor1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminRemoveInstructor", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).removeInstructor("instructor1");
    }

    // ============== Admin Course Management Tests ==============

    @Test
    void testAdminCreateCourseNew() throws Exception {
        setActiveAdminSession();
        when(courseService.getCourse("CS101")).thenReturn(null);
        when(administrativeService.createCourse(any())).thenReturn(testCourse);

        List<String> args = Arrays.asList("--code", "CS101", "--title", "Intro CS", "--credits", "3");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreateCourse", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).createCourse(any(AdminCourseRequest.class));
    }

    @Test
    void testAdminModifyCourseExisting() throws Exception {
        setActiveAdminSession();
        when(courseService.getCourse("CS101")).thenReturn(testCourse);
        when(administrativeService.modifyCourse(any())).thenReturn(testCourse);

        List<String> args = Arrays.asList("--code", "CS101", "--title", "Updated Title");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminModifyCourse", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).modifyCourse(any(AdminCourseRequest.class));
    }

    @Test
    void testAdminRemoveCourse() throws Exception {
        setActiveAdminSession();
        List<String> args = List.of("CS101");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminRemoveCourse", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).removeCourse("CS101");
    }

    // ============== Admin Section Management Tests ==============

    @Test
    void testAdminCreateSection() throws Exception {
        setActiveAdminSession();
        when(courseService.getCourse("CS101")).thenReturn(testCourse);
        when(administrativeService.createSection(any())).thenReturn(testSection);

        List<String> args = Arrays.asList(
                "--course", "CS101",
                "--type", "LECTURE",
                "--enroll-capacity", "30",
                "--waitlist-capacity", "10",
                "--weekday", "M",
                "--start", "09:00",
                "--end", "10:30",
                "--venue", "Room 101");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreateSection", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).createSection(any(AdminSectionService.class));
    }

    @Test
    void testAdminModifySection() throws Exception {
        setActiveAdminSession();
        when(courseService.getCourse("CS101")).thenReturn(testCourse);
        when(administrativeService.modifySection(any())).thenReturn(testSection);

        List<String> args = Arrays.asList(
                "--section-id", "1",
                "--course", "CS101",
                "--enroll-capacity", "35");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminModifySection", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).modifySection(any(AdminSectionService.class));
    }

    @Test
    void testAdminRemoveSection() throws Exception {
        setActiveAdminSession();
        List<String> args = List.of("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminRemoveSection", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).deleteSection(any(AdminSectionService.class));
    }

    // ============== Admin Period Management Tests ==============

    @Test
    void testAdminListPeriods() throws Exception {
        setActiveAdminSession();
        RegistrationPeriod period = mock(RegistrationPeriod.class);
        when(period.getPeriodId()).thenReturn(1);
        when(period.getCohort()).thenReturn(2024);

        when(administrativeService.listRegistrationPeriods(null)).thenReturn(List.of(period));

        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListPeriods", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).listRegistrationPeriods(null);
    }

    @Test
    void testAdminCreatePeriod() throws Exception {
        setActiveAdminSession();
        RegistrationPeriod period = mock(RegistrationPeriod.class);
        when(period.getPeriodId()).thenReturn(1);

        when(administrativeService.createRegistrationPeriod(any())).thenReturn(null);
        when(administrativeService.listRegistrationPeriods(null)).thenReturn(List.of(period));

        List<String> args = Arrays.asList("--cohort", "2024", "--start", "2026-05-01T09:00", "--end", "2026-05-31T17:00");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreatePeriod", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).createRegistrationPeriod(any(AdminPeriodRequest.class));
    }

    @Test
    void testAdminDeletePeriod() throws Exception {
        setActiveAdminSession();
        RegistrationPeriod period = mock(RegistrationPeriod.class);
        when(period.getPeriodId()).thenReturn(1);

        when(administrativeService.listRegistrationPeriods(null)).thenReturn(List.of(period));

        List<String> args = List.of("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminDeletePeriod", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).deleteRegistrationPeriod(1);
    }

    // ============== Admin Instructor Assignment Tests ==============

    @Test
    void testAdminAssignInstructor() throws Exception {
        setActiveAdminSession();
        List<String> args = Arrays.asList("1", "instructor1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAssignInstruction", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).assignInstructor("instructor1", 1);
    }

    @Test
    void testAdminUnassignInstructor() throws Exception {
        setActiveAdminSession();
        List<String> args = Arrays.asList("1", "instructor1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleUnassignInstruction", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).unassignInstructor("instructor1", 1);
    }

    // ============== View Student List Tests ==============

    @Test
    void testViewStudentListByInstructor() throws Exception {
        setActiveInstructorSession();

        // Configure testCourse with an ID or code that TreeMap can use as a key
        when(testCourse.getCourseCode()).thenReturn("CS101");

        when(testSection.getCourse()).thenReturn(testCourse);
        when(testInstructor.getSections()).thenReturn(new HashSet<>(Collections.singletonList(testSection)));

        RegistrationRecord record = mock(RegistrationRecord.class);
        when(record.getStudent()).thenReturn(testStudent);

        when(instructorRepository.findByUserEIDWithSections("instructor1")).thenReturn(Optional.of(testInstructor));
        when(registrationRecordRepository.findBySectionId(0)).thenReturn(new ArrayList<>(List.of(record)));

        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleViewStudentList", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(instructorRepository).findByUserEIDWithSections("instructor1");
    }

    @Test
    void testViewStudentListStudentNotAllowed() throws Exception {
        setActiveStudentSession();
        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleViewStudentList", List.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    // ============== List Courses Tests ==============

    @Test
    void testListCourses() throws Exception {
        when(courseService.getAllCourses()).thenReturn(Collections.singletonList(testCourse));

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleListCourses");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(courseService).getAllCourses();
    }

    @Test
    void testListCoursesEmpty() throws Exception {
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleListCourses");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(courseService).getAllCourses();
    }

    // ============== View Master Schedule Tests ==============

    @Test
    void testViewMasterSchedule() throws Exception {
        setActiveStudentSession();
        when(testCourse.getPrerequisiteCourses()).thenReturn(new HashSet<>());
        when(testCourse.getExclusiveCourses()).thenReturn(new HashSet<>());
        when(testCourse.getSections()).thenReturn(new HashSet<>(Collections.singletonList(testSection)));
        when(testSection.getInstructors()).thenReturn(new HashSet<>(Collections.singletonList(testInstructor)));

        when(courseService.getAllCoursesWithAllData()).thenReturn(Collections.singletonList(testCourse));
        when(registrationRecordRepository.countEnrolled(0)).thenReturn(10);
        when(waitlistRecordRepository.countWaitlisted(0)).thenReturn(2);

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleViewMasterSchedule");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(courseService).getAllCoursesWithAllData();
    }

    @Test
    void testViewMasterScheduleNoCourses() throws Exception {
        setActiveStudentSession();
        when(courseService.getAllCoursesWithAllData()).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleViewMasterSchedule");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(courseService).getAllCoursesWithAllData();
    }

    // ============== Utility Method Tests ==============

    @Test
    void testParseInteger() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseInteger", String.class, String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(cliRunner, "123", "testField");
        assertEquals(123, result);
    }

    @Test
    void testParseIntegerInvalid() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseInteger", String.class, String.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, "notanumber", "testField"));
    }

    @Test
    void testParseDateTime() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseDateTime", String.class, String.class);
        method.setAccessible(true);
        LocalDateTime result = (LocalDateTime) method.invoke(cliRunner, "2026-05-01T09:00", "testField");
        assertNotNull(result);
    }

    @Test
    void testParseDateTimeInvalid() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseDateTime", String.class, String.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, "invalid-date", "testField"));
    }

    @Test
    void testValueOrDash() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("valueOrDash", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(cliRunner, "value");
        assertEquals("value", result);

        result = (String) method.invoke(cliRunner, (String) null);
        assertEquals("-", result);

        result = (String) method.invoke(cliRunner, "   ");
        assertEquals("-", result);
    }

    @Test
    void testFormatDateTime() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("formatDateTime", LocalDateTime.class);
        method.setAccessible(true);
        String result = (String) method.invoke(cliRunner, LocalDateTime.of(2026, 5, 1, 9, 0));  // ← Use cliRunner
        assertNotNull(result);
        assertTrue(result.contains("Fri")); // 2026-05-01 is a Friday

        result = (String) method.invoke(cliRunner, (LocalDateTime) null);  // ← Use cliRunner
        assertEquals("N/A", result);
    }

    @Test
    void testRunProcessesHelpAndQuitCommands() throws Exception {
        when(sessionStore.load()).thenReturn(Optional.empty());

        String originalInput = "help\nquit\n";
        InputStream previousIn = System.in;
        PrintStream previousOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(originalInput.getBytes()));
            System.setOut(new PrintStream(output));

            cliRunner.run();
        } finally {
            System.setIn(previousIn);
            System.setOut(previousOut);
        }

        String console = output.toString();
        assertTrue(console.contains("Course Registration CLI"));
        assertTrue(console.contains("Available commands:"));
        assertTrue(console.contains("CLI session closed."));
        verify(sessionStore).load();
    }

    @Test
    void testHandleLineUnknownCommandPrintsMessage() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream previousOut = System.out;
        try {
            System.setOut(new PrintStream(output));
            invokeHandleLine("unknown-cmd");
        } finally {
            System.setOut(previousOut);
        }

        assertTrue(output.toString().contains("Unknown command. Type help to see available commands."));
    }

    @Test
    void testHandleLineExitSetsRunningFalse() throws Exception {
        setRunning(true);

        invokeHandleLine("exit");

        assertFalse(isRunning());
    }

    @Test
    void testLoadSavedSessionClearsInvalidStoredAdminSession() throws Exception {
        when(sessionStore.load()).thenReturn(Optional.of(new CliSession("admin1", CliRole.ADMIN)));
        when(adminRepository.findByUserEID("admin1")).thenReturn(Optional.empty());

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("loadSavedSession");
        method.setAccessible(true);
        method.invoke(cliRunner);

        verify(sessionStore).clear();
    }

    @Test
    void testLoadSavedSessionRestoresValidInstructorSession() throws Exception {
        when(sessionStore.load()).thenReturn(Optional.of(new CliSession("instructor1", CliRole.INSTRUCTOR)));
        when(instructorRepository.findByUserEID("instructor1")).thenReturn(Optional.of(testInstructor));

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("loadSavedSession");
        method.setAccessible(true);
        method.invoke(cliRunner);

        java.lang.reflect.Field field = InteractiveCliRunner.class.getDeclaredField("activeSession");
        field.setAccessible(true);
        CliSession restored = (CliSession) field.get(cliRunner);
        assertNotNull(restored);
        assertEquals("instructor1", restored.getUserEid());
        assertEquals(CliRole.INSTRUCTOR, restored.getRole());
    }

    @Test
    void testShowTimetableRejectsUnexpectedArguments() throws Exception {
        setActiveStudentSession();
        List<String> args = List.of("unexpected");

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleShowTimeTable", List.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testShowTimetableRequiresAuthentication() throws Exception {
        List<String> args = Collections.emptyList();

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleShowTimeTable", List.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testExportTimetableRequiresAuthentication() throws Exception {
        List<String> args = Collections.emptyList();

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleExportTimetable", List.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testExportTimetableRejectsTooManyArguments() throws Exception {
        setActiveStudentSession();
        List<String> args = Arrays.asList("a.txt", "b.txt");

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleExportTimetable", List.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testValidateCourseOptionsThrowsForUnknownOption() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put("code", "CS101");
        options.put("unknown", "x");

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("validateCourseOptions", Map.class, String.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, options, "admin-create-course"));
    }

    @Test
    void testSplitCsvReturnsEmptySetForOnlyBlankElements() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("splitCsv", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) method.invoke(cliRunner, " ,  , ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAdminCreateSectionRequiresVenue() throws Exception {
        setActiveAdminSession();
        List<String> args = Arrays.asList(
                "--course", "CS101",
                "--type", "LECTURE",
                "--enroll-capacity", "30",
                "--waitlist-capacity", "10",
                "--weekday", "M",
                "--start", "09:00",
                "--end", "10:30"
                );

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreateSection", List.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testAdminCreateSectionRejectsUnknownCourse() throws Exception {
        setActiveAdminSession();
        when(courseService.getCourse("CS999")).thenReturn(null);

        List<String> args = Arrays.asList(
                "--course", "CS999",
                "--type", "LECTURE",
                "--enroll-capacity", "30",
                "--waitlist-capacity", "10",
                "--weekday", "M",
                "--start", "09:00",
                "--end", "10:30",
                "--venue", "Room 101");

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminCreateSection", List.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testAdminModifySectionRequiresSectionId() throws Exception {
        setActiveAdminSession();
        when(courseService.getCourse("CS101")).thenReturn(testCourse);

        List<String> args = Arrays.asList("--course", "CS101", "--enroll-capacity", "20");

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminModifySection", List.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testViewStudentListAdminRequiresInstructorOption() throws Exception {
        setActiveAdminSession();
        List<String> args = Collections.emptyList();

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleViewStudentList", List.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
    }

    @Test
    void testViewStudentListAdminUsesSpecifiedInstructor() throws Exception {
        setActiveAdminSession();
        when(testInstructor.getSections()).thenReturn(Collections.emptySet());
        when(instructorRepository.findByUserEIDWithSections("instructor1")).thenReturn(Optional.of(testInstructor));

        List<String> args = Arrays.asList("--instructor", "instructor1");

        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleViewStudentList", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(instructorRepository).findByUserEIDWithSections("instructor1");
    }

    @Test
    void testSectionTimeLabelReturnsDashWhenTimeMissing() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("sectionTimeLabel", Section.class);
        method.setAccessible(true);

        String result = (String) method.invoke(cliRunner, testSection);

        assertEquals("-", result);
    }

    @Test
    void testRunContinuesAfterCommandErrorUntilQuit() throws Exception {
        when(sessionStore.load()).thenReturn(Optional.empty());
        when(adminRepository.findByUserEID("invalid")).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID("invalid")).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID("invalid")).thenReturn(Optional.empty());

        String originalInput = "login invalid password\nquit\n";
        InputStream previousIn = System.in;
        PrintStream previousOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(originalInput.getBytes()));
            System.setOut(new PrintStream(output));

            cliRunner.run();
        } finally {
            System.setIn(previousIn);
            System.setOut(previousOut);
        }

        String console = output.toString();
        assertTrue(console.contains("ERROR: Invalid credentials"));
        assertTrue(console.contains("CLI session closed."));
    }

    // ============== Helper Methods ==============

    private void setActiveStudentSession() throws Exception {
        CliSession session = new CliSession("student1", CliRole.STUDENT);
        java.lang.reflect.Field field = InteractiveCliRunner.class.getDeclaredField("activeSession");
        field.setAccessible(true);
        field.set(cliRunner, session);
    }

    private void setActiveInstructorSession() throws Exception {
        CliSession session = new CliSession("instructor1", CliRole.INSTRUCTOR);
        java.lang.reflect.Field field = InteractiveCliRunner.class.getDeclaredField("activeSession");
        field.setAccessible(true);
        field.set(cliRunner, session);
    }

    private void setActiveAdminSession() throws Exception {
        CliSession session = new CliSession("admin1", CliRole.ADMIN);
        java.lang.reflect.Field field = InteractiveCliRunner.class.getDeclaredField("activeSession");
        field.setAccessible(true);
        field.set(cliRunner, session);
    }

    private void setRunning(boolean value) throws Exception {
        java.lang.reflect.Field field = InteractiveCliRunner.class.getDeclaredField("running");
        field.setAccessible(true);
        field.set(cliRunner, value);
    }

    private boolean isRunning() throws Exception {
        java.lang.reflect.Field field = InteractiveCliRunner.class.getDeclaredField("running");
        field.setAccessible(true);
        return (boolean) field.get(cliRunner);
    }

    private void invokeHandleLine(String line) throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleLine", String.class);
        method.setAccessible(true);
        method.invoke(cliRunner, line);
    }

    // ============== parseIntegerCsv Tests ==============

    @Test
    void testParseIntegerCsvValid() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "1,2,3", "testField");

        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    @Test
    void testParseIntegerCsvWithWhitespace() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "1 , 2 , 3", "testField");

        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    @Test
    void testParseIntegerCsvNull() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, null, "testField");

        assertTrue(result.isEmpty());
    }

    @Test
    void testParseIntegerCsvBlank() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "   ", "testField");

        assertTrue(result.isEmpty());
    }

    @Test
    void testParseIntegerCsvEmpty() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "", "testField");

        assertTrue(result.isEmpty());
    }

    @Test
    void testParseIntegerCsvSingleValue() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "42", "testField");

        assertEquals(1, result.size());
        assertTrue(result.contains(42));
    }

    @Test
    void testParseIntegerCsvDuplicates() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "1,2,2,3,3,3", "testField");

        // Set should deduplicate
        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    @Test
    void testParseIntegerCsvWithEmptyElements() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "1,,2,,3", "testField");

        // Empty elements should be filtered out
        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    @Test
    void testParseIntegerCsvInvalidValue() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        // Should throw exception when parseInteger encounters invalid value
        assertThrows(Exception.class, () -> method.invoke(cliRunner, "1,notanumber,3", "testField"));
    }

    @Test
    void testParseIntegerCsvLargeNumbers() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "1000000,2000000,3000000", "testField");

        assertEquals(3, result.size());
        assertTrue(result.contains(1000000));
        assertTrue(result.contains(2000000));
        assertTrue(result.contains(3000000));
    }
}
