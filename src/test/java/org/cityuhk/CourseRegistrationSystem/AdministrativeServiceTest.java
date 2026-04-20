package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Exception.InvalidNameException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidPasswordException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidUserEIDException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserEidAlreadyExistsException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserNotFoundException;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.InstructorUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.StudentUserRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.CourseRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.AdminUserManagementOperations;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.InstructorUserManagementOperations;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.StudentUserManagementOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministrativeServiceTest {

    @Mock private AdminUserManagementOperations adminUserManagementService;
    @Mock private StudentUserManagementOperations studentUserManagementService;
    @Mock private InstructorUserManagementOperations instructorUserManagementService;
    @Mock private CourseRepository courseRepository;
    @InjectMocks private AdministrativeService service;

    private AdminUserRequest userReq;
    private StudentUserRequest studentReq;
    private InstructorUserRequest instructorReq;
    private AdminCourseRequest courseReq;
    private Admin admin;
    private Student student;
    private Instructor instructor;
    private Course course;
    private Course prereqCourse;

    @BeforeEach
    void setUp() {
        userReq = new AdminUserRequest();
        userReq.setUserEID("EID123");
        userReq.setName("Test Admin");
        userReq.setPassword("pass123");

        studentReq = new StudentUserRequest();
        studentReq.setUserEID("SEID123");
        studentReq.setName("Test Student");
        studentReq.setPassword("pass456");

        instructorReq = new InstructorUserRequest();
        instructorReq.setUserEID("IEID123");
        instructorReq.setName("Test Instructor");
        instructorReq.setPassword("pass789");

        courseReq = new AdminCourseRequest();
        courseReq.setCourseCode("CS101");
        courseReq.setTitle("Intro CS");
        courseReq.setCredits(3);

        admin = new Admin.AdminBuilder().withStaffId(1).withUserEID("EID123").withName("Test").withPassword("enc").build();
        student = new Student.StudentBuilder().withStudentId(1).withUserEID("SEID123").withName("Student").withPassword("enc").build();
        instructor = new Instructor.InstructorBuilder().withStaffId(1).withUserEID("IEID123").withName("Instructor").withPassword("enc").build();
        course = new Course("CS101", "Intro CS", 3, null, null, Set.of(), Set.of(), null);
        prereqCourse = new Course("CS102", "Prereq CS", 3, null, null, Set.of(), Set.of(), null);
    }

    @Test
    void listUsers_success() {
        when(adminUserManagementService.listUsers()).thenReturn(List.of(admin));
        assertEquals(1, service.listUsers().size());
    }

    @Test
    void createUser_blankEID_throws() {
        userReq.setUserEID("");
        when(adminUserManagementService.createUser(userReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_blankName_throws() {
        userReq.setName("");
        when(adminUserManagementService.createUser(userReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_blankPassword_throws() {
        userReq.setPassword("");
        when(adminUserManagementService.createUser(userReq)).thenThrow(new InvalidPasswordException());
        assertThrows(InvalidPasswordException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_duplicateEID_throws() {
        when(adminUserManagementService.createUser(userReq)).thenThrow(new UserEidAlreadyExistsException(userReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_success() {
        when(adminUserManagementService.createUser(userReq)).thenReturn(admin);
        assertNotNull(service.createUser(userReq));
    }

    @Test
    void modifyUser_notFound_throws() {
        when(adminUserManagementService.modifyUser(99, userReq)).thenThrow(new UserNotFoundException("Admin", 99));
        assertThrows(UserNotFoundException.class, () -> service.modifyUser(99, userReq));
    }

    @Test
    void modifyUser_blankEID_throws() {
        userReq.setUserEID("");
        when(adminUserManagementService.modifyUser(1, userReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_blankName_throws() {
        userReq.setName("");
        when(adminUserManagementService.modifyUser(1, userReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_duplicateEID_throws() {
        when(adminUserManagementService.modifyUser(1, userReq)).thenThrow(new UserEidAlreadyExistsException(userReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_keepOldPassword_success() {
        userReq.setPassword("");
        when(adminUserManagementService.modifyUser(1, userReq)).thenReturn(admin);
        assertNotNull(service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_success() {
        when(adminUserManagementService.modifyUser(1, userReq)).thenReturn(admin);
        assertNotNull(service.modifyUser(1, userReq));
    }

    @Test
    void removeUser_notFound_throws() {
        doThrow(new UserNotFoundException("Admin", 99)).when(adminUserManagementService).removeUser(99);
        assertThrows(UserNotFoundException.class, () -> service.removeUser(99));
    }

    @Test
    void removeUser_success() {
        doNothing().when(adminUserManagementService).removeUser(1);
        service.removeUser(1);
        verify(adminUserManagementService).removeUser(1);
    }

    @Test
    void createCourse_blankCode_throws() {
        courseReq.setCourseCode("");
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_blankTitle_throws() {
        courseReq.setTitle("");
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_creditsZero_throws() {
        courseReq.setCredits(0);
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_duplicateCode_throws() {
        when(courseRepository.existsByCourseCode(anyString())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_selfPrereq_throws() {
        courseReq.setPrerequisiteCourseCodes(Set.of("CS101"));
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_selfExclusive_throws() {
        courseReq.setExclusiveCourseCodes(Set.of("CS101"));
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_success() {
        when(courseRepository.existsByCourseCode(anyString())).thenReturn(false);
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.createCourse(courseReq));
    }

    @Test
    void modifyCourse_blankCode_throws() {
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_notFound_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_duplicateNewCode_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        when(courseRepository.existsByCourseCode(anyString())).thenReturn(true);
        courseReq.setCourseCode("CS999");
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_blankTitle_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        courseReq.setTitle("");
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_creditsNegative_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        courseReq.setCredits(-1);
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_selfPrereq_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        courseReq.setPrerequisiteCourseCodes(Set.of("CS101"));
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_selfExclusive_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        courseReq.setExclusiveCourseCodes(Set.of("CS101"));
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_success() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_descriptionUpdated_success() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        courseReq.setDescription("New desc");
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_termUpdated_success() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        courseReq.setTerm("2026A");
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_creditsZero_skipsUpdate_success() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        courseReq.setCredits(0);
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_prereqUpdated_success() {
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(course));
        when(courseRepository.findByCourseCode("CS102")).thenReturn(Optional.of(prereqCourse));
        courseReq.setPrerequisiteCourseCodes(Set.of("CS102"));
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_exclusiveUpdated_success() {
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(course));
        when(courseRepository.findByCourseCode("CS102")).thenReturn(Optional.of(prereqCourse));
        courseReq.setExclusiveCourseCodes(Set.of("CS102"));
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.modifyCourse(courseReq));
    }

    @Test
    void removeCourse_blankCode_throws() {
        assertThrows(RuntimeException.class, () -> service.removeCourse(""));
    }

    @Test
    void removeCourse_notFound_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.removeCourse("CS101"));
    }

    @Test
    void removeCourse_success() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        service.removeCourse("CS101");
        verify(courseRepository).delete(course);
    }

    @Test
    void resolveCourseCodes_notFound_throws() {
        courseReq.setPrerequisiteCourseCodes(Set.of("INVALID"));
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    // ── Student create/modify/remove tests ────────────────────────────────────

    @Test
    void createStudent_blankEID_throws() {
        studentReq.setUserEID("");
        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.createStudent(studentReq));
    }

    @Test
    void createStudent_blankName_throws() {
        studentReq.setName("");
        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.createStudent(studentReq));
    }

    @Test
    void createStudent_blankPassword_throws() {
        studentReq.setPassword("");
        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new InvalidPasswordException());
        assertThrows(InvalidPasswordException.class, () -> service.createStudent(studentReq));
    }

    @Test
    void createStudent_duplicateEIDInStudentRepo_throws() {
        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new UserEidAlreadyExistsException(studentReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createStudent(studentReq));
    }

    @Test
    void createStudent_duplicateEIDInAdminRepo_throws() {
        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new UserEidAlreadyExistsException(studentReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createStudent(studentReq));
    }

    @Test
    void createStudent_duplicateEIDInInstructorRepo_throws() {
        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new UserEidAlreadyExistsException(studentReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createStudent(studentReq));
    }

    @Test
    void createStudent_success() {
        when(studentUserManagementService.createStudent(studentReq)).thenReturn(student);
        assertNotNull(service.createStudent(studentReq));
    }

    @Test
    void modifyStudent_notFound_throws() {
        when(studentUserManagementService.modifyStudent(99, studentReq)).thenThrow(new UserNotFoundException("Student", 99));
        assertThrows(UserNotFoundException.class, () -> service.modifyStudent(99, studentReq));
    }

    @Test
    void modifyStudent_blankEID_throws() {
        studentReq.setUserEID("");
        when(studentUserManagementService.modifyStudent(1, studentReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.modifyStudent(1, studentReq));
    }

    @Test
    void modifyStudent_blankName_throws() {
        studentReq.setName("");
        when(studentUserManagementService.modifyStudent(1, studentReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.modifyStudent(1, studentReq));
    }

    @Test
    void modifyStudent_duplicateEID_throws() {
        when(studentUserManagementService.modifyStudent(1, studentReq)).thenThrow(new UserEidAlreadyExistsException(studentReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.modifyStudent(1, studentReq));
    }

    @Test
    void modifyStudent_success() {
        when(studentUserManagementService.modifyStudent(1, studentReq)).thenReturn(student);
        assertNotNull(service.modifyStudent(1, studentReq));
    }

    @Test
    void removeStudent_notFound_throws() {
        doThrow(new UserNotFoundException("Student", 99)).when(studentUserManagementService).removeStudent(99);
        assertThrows(UserNotFoundException.class, () -> service.removeStudent(99));
    }

    @Test
    void removeStudent_success() {
        doNothing().when(studentUserManagementService).removeStudent(1);
        service.removeStudent(1);
        verify(studentUserManagementService).removeStudent(1);
    }

    // ── Instructor create/modify/remove tests ─────────────────────────────────

    @Test
    void createInstructor_blankEID_throws() {
        instructorReq.setUserEID("");
        when(instructorUserManagementService.createInstructor(instructorReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.createInstructor(instructorReq));
    }

    @Test
    void createInstructor_blankName_throws() {
        instructorReq.setName("");
        when(instructorUserManagementService.createInstructor(instructorReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.createInstructor(instructorReq));
    }

    @Test
    void createInstructor_blankPassword_throws() {
        instructorReq.setPassword("");
        when(instructorUserManagementService.createInstructor(instructorReq)).thenThrow(new InvalidPasswordException());
        assertThrows(InvalidPasswordException.class, () -> service.createInstructor(instructorReq));
    }

    @Test
    void createInstructor_duplicateEIDInAdminRepo_throws() {
        when(instructorUserManagementService.createInstructor(instructorReq)).thenThrow(new UserEidAlreadyExistsException(instructorReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createInstructor(instructorReq));
    }

    @Test
    void createInstructor_duplicateEIDInStudentRepo_throws() {
        when(instructorUserManagementService.createInstructor(instructorReq)).thenThrow(new UserEidAlreadyExistsException(instructorReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createInstructor(instructorReq));
    }

    @Test
    void createInstructor_duplicateEIDInInstructorRepo_throws() {
        when(instructorUserManagementService.createInstructor(instructorReq)).thenThrow(new UserEidAlreadyExistsException(instructorReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createInstructor(instructorReq));
    }

    @Test
    void createInstructor_success() {
        when(instructorUserManagementService.createInstructor(instructorReq)).thenReturn(instructor);
        assertNotNull(service.createInstructor(instructorReq));
    }

    @Test
    void modifyInstructor_notFound_throws() {
        when(instructorUserManagementService.modifyInstructor(99, instructorReq)).thenThrow(new UserNotFoundException("Instructor", 99));
        assertThrows(UserNotFoundException.class, () -> service.modifyInstructor(99, instructorReq));
    }

    @Test
    void modifyInstructor_blankEID_throws() {
        instructorReq.setUserEID("");
        when(instructorUserManagementService.modifyInstructor(1, instructorReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.modifyInstructor(1, instructorReq));
    }

    @Test
    void modifyInstructor_blankName_throws() {
        instructorReq.setName("");
        when(instructorUserManagementService.modifyInstructor(1, instructorReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.modifyInstructor(1, instructorReq));
    }

    @Test
    void modifyInstructor_duplicateEID_throws() {
        when(instructorUserManagementService.modifyInstructor(1, instructorReq)).thenThrow(new UserEidAlreadyExistsException(instructorReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.modifyInstructor(1, instructorReq));
    }

    @Test
    void modifyInstructor_success() {
        when(instructorUserManagementService.modifyInstructor(1, instructorReq)).thenReturn(instructor);
        assertNotNull(service.modifyInstructor(1, instructorReq));
    }

    @Test
    void removeInstructor_notFound_throws() {
        doThrow(new UserNotFoundException("Instructor", 99)).when(instructorUserManagementService).removeInstructor(99);
        assertThrows(UserNotFoundException.class, () -> service.removeInstructor(99));
    }

    @Test
    void removeInstructor_success() {
        doNothing().when(instructorUserManagementService).removeInstructor(1);
        service.removeInstructor(1);
        verify(instructorUserManagementService).removeInstructor(1);
    }

    // ── Cross-role EID uniqueness ──────────────────────────────────────────────

    @Test
    void createAdmin_duplicateEIDInStudentRepo_throws() {
        when(adminUserManagementService.createUser(userReq)).thenThrow(new UserEidAlreadyExistsException(userReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createUser(userReq));
    }

    @Test
    void createAdmin_duplicateEIDInInstructorRepo_throws() {
        when(adminUserManagementService.createUser(userReq)).thenThrow(new UserEidAlreadyExistsException(userReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createUser(userReq));
    }
}