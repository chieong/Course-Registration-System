package org.cityuhk.CourseRegistrationSystem.Cli;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
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
        List<String> args = Arrays.asList("only_one_arg");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleLogin", List.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(cliRunner, args));
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

        List<String> args = Arrays.asList("1");
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

        List<String> args = Arrays.asList("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleDropSection", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationService).dropSection(eq(0), eq(1), any(LocalDateTime.class));
    }

    @Test
    void testJoinWaitlist() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = Arrays.asList("1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleJoinWaitlist", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(registrationService).waitListSection(eq(0), eq(1), any(LocalDateTime.class));
    }

    @Test
    void testDropWaitlist() throws Exception {
        setActiveStudentSession();
        when(studentRepository.findByUserEID("student1")).thenReturn(Optional.of(testStudent));

        List<String> args = Arrays.asList("1");
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

        when(registrationPlanService.getPlanSet(0)).thenReturn(Arrays.asList(plan));

        List<String> args = Collections.emptyList();
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

        List<String> args = Arrays.asList("2");
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

        List<String> args = Arrays.asList("1");
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
                .thenReturn(Arrays.asList(plan1));

        List<String> args = Arrays.asList("1,2");
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
        when(administrativeService.listUsers()).thenReturn(Arrays.asList(testAdmin));

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
        List<String> args = Arrays.asList("admin1");
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
        List<String> args = Arrays.asList("student1");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminRemoveStudent", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).removeStudent("student1");
    }

    // ============== Admin Instructor Management Tests ==============

    @Test
    void testAdminListInstructors() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listInstructors()).thenReturn(Arrays.asList(testInstructor));

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
        List<String> args = Arrays.asList("instructor1");
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
        List<String> args = Arrays.asList("CS101");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminRemoveCourse", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).removeCourse("CS101");
    }

    // ============== Admin Section Management Tests ==============

    @Test
    void testAdminListSections() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listSections(null)).thenReturn(Arrays.asList(testSection));

        List<String> args = Collections.emptyList();
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListSections", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).listSections(null);
    }

    @Test
    void testAdminListSectionsByCode() throws Exception {
        setActiveAdminSession();
        when(administrativeService.listSections("CS101")).thenReturn(Arrays.asList(testSection));

        List<String> args = Arrays.asList("--course", "CS101");
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("handleAdminListSections", List.class);
        method.setAccessible(true);
        method.invoke(cliRunner, args);

        verify(administrativeService).listSections("CS101");
    }

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
                "--start", "2026-05-01T09:00",
                "--end", "2026-05-01T10:30",
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
        List<String> args = Arrays.asList("1");
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

        when(administrativeService.listRegistrationPeriods(null)).thenReturn(Arrays.asList(period));

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
        when(administrativeService.listRegistrationPeriods(null)).thenReturn(Arrays.asList(period));

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

        when(administrativeService.listRegistrationPeriods(null)).thenReturn(Arrays.asList(period));

        List<String> args = Arrays.asList("1");
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
        when(testInstructor.getSections()).thenReturn(new HashSet<>(Arrays.asList(testSection)));

        RegistrationRecord record = mock(RegistrationRecord.class);
        when(record.getStudent()).thenReturn(testStudent);

        when(instructorRepository.findByUserEIDWithSections("instructor1")).thenReturn(Optional.of(testInstructor));
        when(registrationRecordRepository.findBySectionId(0)).thenReturn(new ArrayList<>(Arrays.asList(record)));

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
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(testCourse));

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
        when(testCourse.getSections()).thenReturn(new HashSet<>(Arrays.asList(testSection)));
        when(testSection.getInstructors()).thenReturn(new HashSet<>(Arrays.asList(testInstructor)));

        when(courseService.getAllCoursesWithAllData()).thenReturn(Arrays.asList(testCourse));
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
        assertTrue(result.contains("2026"));

        result = (String) method.invoke(cliRunner, (LocalDateTime) null);  // ← Use cliRunner
        assertEquals("N/A", result);
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
    void testParseIntegerCsvNegativeValues() throws Exception {
        java.lang.reflect.Method method = InteractiveCliRunner.class.getDeclaredMethod("parseIntegerCsv", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(cliRunner, "-1,-2,-3", "testField");

        assertEquals(3, result.size());
        assertTrue(result.contains(-1));
        assertTrue(result.contains(-2));
        assertTrue(result.contains(-3));
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