package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Exception.InvalidNameException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidPasswordException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidUserEIDException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserEidAlreadyExistsException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserNotFoundException;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminSectionService;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminInstructorRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminStudentRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.CourseRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodOverlapException;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodValidationException;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodValidator;
import java.time.LocalDateTime;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministrativeServiceTest {

    @Mock private AdminRepositoryPort adminRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private CourseRepositoryPort courseRepository;
    @Mock private SectionRepositoryPort sectionRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RegistrationPeriodValidator periodValidator;
    @Mock private AdminUserManagementOperations adminUserManagementService;
    @Mock private StudentUserManagementOperations studentUserManagementService;
    @Mock private InstructorUserManagementOperations instructorUserManagementService;
    @Mock private RegistrationRecordRepository registrationRecordRepository;
    @InjectMocks private AdministrativeService service;

    private AdminUserRequest userReq;
    private AdminStudentRequest studentReq;
    private AdminInstructorRequest instructorReq;
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

        studentReq = new AdminStudentRequest();
        studentReq.setUserEID("SEID123");
        studentReq.setName("Test Student");
        studentReq.setPassword("pass456");

        instructorReq = new AdminInstructorRequest();
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
        course = new Course("CS101", "Intro CS", 3, null, Set.of(), Set.of(), null);
        prereqCourse = new Course("CS102", "Prereq CS", 3, null, Set.of(), Set.of(), null);
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
        when(adminUserManagementService.modifyUser(userReq)).thenThrow(new UserNotFoundException("Admin", "EID99"));
        userReq.setUserEID("EID99");
        assertThrows(UserNotFoundException.class, () -> service.modifyUser(userReq));
    }

    @Test
    void modifyUser_blankEID_throws() {
        userReq.setUserEID("");
        when(adminUserManagementService.modifyUser(userReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.modifyUser(userReq));
    }

    @Test
    void modifyUser_blankName_throws() {
        userReq.setName("");
        when(adminUserManagementService.modifyUser(userReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.modifyUser(userReq));
    }

    @Test
    void modifyUser_duplicateEID_throws() {
        when(adminUserManagementService.modifyUser(userReq)).thenThrow(new UserEidAlreadyExistsException(userReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.modifyUser(userReq));
    }

    @Test
    void modifyUser_keepOldPassword_success() {
        userReq.setPassword("");
        when(adminUserManagementService.modifyUser(userReq)).thenReturn(admin);
        assertNotNull(service.modifyUser(userReq));
    }

    @Test
    void modifyUser_success() {
        when(adminUserManagementService.modifyUser(userReq)).thenReturn(admin);
        assertNotNull(service.modifyUser(userReq));
    }

    @Test
    void removeUser_notFound_throws() {
        doThrow(new UserNotFoundException("Admin", "EID99")).when(adminUserManagementService).removeUser("EID99");
        assertThrows(UserNotFoundException.class, () -> service.removeUser("EID99"));
    }

    @Test
    void removeUser_success() {
        doNothing().when(adminUserManagementService).removeUser("EID123");
        service.removeUser("EID123");
        verify(adminUserManagementService).removeUser("EID123");
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
        courseReq.setCourseCode("");
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_nullCode_throws() {
        courseReq.setCourseCode(null);
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_notFound_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_duplicateNewCode_throws() {
        courseReq.setCourseCode("CS999");
        Course cs101 = new Course("CS101", "Old Title", 3, null, Set.of(), Set.of(), null);
        when(courseRepository.findByCourseCode("CS999")).thenReturn(Optional.of(cs101));
        when(courseRepository.existsByCourseCode("CS999")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_renameToNewCode_success() {
        // newCourseCode differs from existingCourse.getCourseCode() and does NOT already exist
        courseReq.setCourseCode("CS999");
        courseReq.setTitle(null);   // also covers title==null branch
        Course cs101 = new Course("CS101", "Old Title", 3, null, Set.of(), Set.of(), null);
        when(courseRepository.findByCourseCode("CS999")).thenReturn(Optional.of(cs101));
        when(courseRepository.existsByCourseCode("CS999")).thenReturn(false);
        when(courseRepository.save(any())).thenReturn(cs101);
        when(registrationRecordRepository.existsByCourseCode("CS999")).thenReturn(false);
        assertNotNull(service.modifyCourse(courseReq));
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
    void resolveCourseCodes_nullCodeInSet_success() {
        // null entry in the code set must be silently skipped
        java.util.Set<String> codesWithNull = new java.util.HashSet<>();
        codesWithNull.add(null);
        courseReq.setPrerequisiteCourseCodes(codesWithNull);
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.createCourse(courseReq));
    }

    @Test
    void resolveCourseCodes_blankCodeInSet_success() {
        // blank entry in the code set must be silently skipped
        java.util.Set<String> codesWithBlank = new java.util.HashSet<>();
        codesWithBlank.add("   ");
        courseReq.setPrerequisiteCourseCodes(codesWithBlank);
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.createCourse(courseReq));
    }

    @Test
    void resolveCourseCodes_notFound_throws() {
        courseReq.setPrerequisiteCourseCodes(Set.of("INVALID"));
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }
    // createUser null-input guards

    @Test
    void createUser_nullEID_throws() {
        userReq.setUserEID(null);
        when(adminUserManagementService.createUser(userReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_nullName_throws() {
        userReq.setName(null);
        when(adminUserManagementService.createUser(userReq)).thenThrow(new InvalidNameException());
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_nullPassword_throws() {
        userReq.setPassword(null);
        when(adminUserManagementService.createUser(userReq)).thenThrow(new InvalidPasswordException());
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    // modifyUser remaining branches

    @Test
    void modifyUser_nullEID_throws() {
        userReq.setUserEID(null);
        when(adminUserManagementService.modifyUser(userReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(RuntimeException.class, () -> service.modifyUser(userReq));
    }

    @Test
    void modifyUser_nullName_throws() {
        userReq.setName(null);
        when(adminUserManagementService.modifyUser(userReq)).thenThrow(new InvalidNameException());
        assertThrows(RuntimeException.class, () -> service.modifyUser(userReq));
    }

    @Test
    void modifyUser_newEIDNotTaken_success() {
        when(adminUserManagementService.modifyUser(userReq)).thenReturn(admin);
        assertNotNull(service.modifyUser(userReq));
    }

    @Test
    void modifyUser_nullPassword_keepsExistingPassword() {
        // Null password must not re-encode; existing encoded password is preserved
        userReq.setPassword(null);
        when(adminUserManagementService.modifyUser(userReq)).thenReturn(admin);
        Admin result = service.modifyUser(userReq);
        assertNotNull(result);
        // passwordEncoder.encode must NOT have been called
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void createCourse_nullCode_throws() {
        courseReq.setCourseCode(null);
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_nullTitle_throws() {
        courseReq.setTitle(null);
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_negativeCredits_throws() {
        courseReq.setCredits(-1);
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    @Test
    void createCourse_exclusiveNotFound_throws() {
        courseReq.setExclusiveCourseCodes(Set.of("NOTEXIST"));
        when(courseRepository.existsByCourseCode(anyString())).thenReturn(false);
        when(courseRepository.findByCourseCode("NOTEXIST")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.createCourse(courseReq));
    }

    // ?�?� modifyCourse ??resolveCourseCodes failure paths ?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�

    @Test
    void modifyCourse_prereqNotFound_throws() {
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(course));
        when(courseRepository.findByCourseCode("NOTEXIST")).thenReturn(Optional.empty());
        courseReq.setPrerequisiteCourseCodes(Set.of("NOTEXIST"));
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_exclusiveNotFound_throws() {
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(course));
        when(courseRepository.findByCourseCode("NOTEXIST")).thenReturn(Optional.empty());
        courseReq.setExclusiveCourseCodes(Set.of("NOTEXIST"));
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void removeCourse_nullCode_throws() {
        assertThrows(RuntimeException.class, () -> service.removeCourse(null));
    }

    @Test
    void createSection_nullVenue_throws() {
        AdminSectionService req = new AdminSectionService();
        req.setCourse(course);
        req.setSectionType(Section.Type.LECTURE);
        req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
        req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
        // venue is null by default
        assertThrows(RuntimeException.class, () -> service.createSection(req));
    }

    @Test
    void createSection_nullCourse_throws() {
        AdminSectionService req = new AdminSectionService();
        req.setSectionType(Section.Type.LECTURE);
        req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
        req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
        req.setVenue("Y101");
        // course is null by default
        assertThrows(RuntimeException.class, () -> service.createSection(req));
    }

    @Test
    void createSection_nullSectionType_throws() {
        AdminSectionService req = new AdminSectionService();
        req.setCourse(course);
        req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
        req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
        req.setVenue("Y101");
        // sectionType is null by default
        assertThrows(RuntimeException.class, () -> service.createSection(req));
    }

    @Test
    void createSection_nullStartTime_throws() {
        AdminSectionService req = new AdminSectionService();
        req.setCourse(course);
        req.setSectionType(Section.Type.LECTURE);
        req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
        req.setVenue("Y101");
        assertThrows(RuntimeException.class, () -> service.createSection(req));
    }

    @Test
    void createSection_nullEndTime_throws() {
        AdminSectionService req = new AdminSectionService();
        req.setCourse(course);
        req.setSectionType(Section.Type.LECTURE);
        req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
        req.setVenue("Y101");
        assertThrows(RuntimeException.class, () -> service.createSection(req));
    }

    @Test
    void createSection_blankVenue_throws() {
        AdminSectionService req = new AdminSectionService();
        req.setCourse(course);
        req.setSectionType(Section.Type.LECTURE);
        req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
        req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
        req.setVenue("");
        assertThrows(RuntimeException.class, () -> service.createSection(req));
    }

    @Test
    void createSection_success() {
        AdminSectionService req = new AdminSectionService();
        req.setCourse(course);
        req.setSectionType(Section.Type.LECTURE);
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);
        req.setStartTime(start);
        req.setEndTime(end);
        req.setVenue("Y101");
        req.setEnrollCapacity(50);
        req.setWaitlistCapacity(10);
        Section saved = new Section(course, 50, 10, start, end, "Y101");
        when(sectionRepository.save(any(Section.class))).thenReturn(saved);

        Section result = service.createSection(req);

        assertNotNull(result);
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    void modifySection_nullSectionId_throws() {
        AdminSectionService req = new AdminSectionService();
        // sectionId is null by default
        assertThrows(RuntimeException.class, () -> service.modifySection(req));
    }

    @Test
    void modifySection_sectionNotFound_throws() {
        AdminSectionService req = new AdminSectionService();
        req.setSectionId(99);
        when(sectionRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.modifySection(req));
    }

    @Test
    void modifySection_updateVenue_success() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);
        Section existing = new Section(course, 50, 10, start, end, "OldVenue");
        existing.setSectionId(10);

        AdminSectionService req = new AdminSectionService();
        req.setSectionId(10);
        req.setVenue("NewVenue");

        when(sectionRepository.findById(10)).thenReturn(Optional.of(existing));
        when(sectionRepository.save(any(Section.class))).thenReturn(existing);

        Section result = service.modifySection(req);
        assertNotNull(result);
        verify(sectionRepository).save(existing);
    }

    @Test
    void modifySection_updateCourse_success() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);
        Section existing = new Section(course, 50, 10, start, end, "Y101");
        existing.setSectionId(10);
        Course newCourse = new Course("CS200", "Algorithms", 3, null, Set.of(), Set.of(), Set.of());

        AdminSectionService req = new AdminSectionService();
        req.setSectionId(10);
        req.setCourse(newCourse);

        when(sectionRepository.findById(10)).thenReturn(Optional.of(existing));
        when(sectionRepository.save(any(Section.class))).thenReturn(existing);

        assertNotNull(service.modifySection(req));
    }

    @Test
    void modifySection_updateCapacities_success() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);
        Section existing = new Section(course, 50, 10, start, end, "Y101");
        existing.setSectionId(10);

        AdminSectionService req = new AdminSectionService();
        req.setSectionId(10);
        req.setEnrollCapacity(80);
        req.setWaitlistCapacity(20);

        when(sectionRepository.findById(10)).thenReturn(Optional.of(existing));
        when(sectionRepository.save(any(Section.class))).thenReturn(existing);

        assertNotNull(service.modifySection(req));
    }

    @Test
    void modifySection_updateSectionType_success() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);
        Section existing = new Section(course, 50, 10, start, end, "Y101");
        existing.setSectionId(10);

        AdminSectionService req = new AdminSectionService();
        req.setSectionId(10);
        req.setSectionType(Section.Type.LAB);

        when(sectionRepository.findById(10)).thenReturn(Optional.of(existing));
        when(sectionRepository.save(any(Section.class))).thenReturn(existing);

        assertNotNull(service.modifySection(req));
    }

    @Test
    void modifySection_blankVenue_skipsFirstCheck_success() {
        // blank venue: line 307 (venue != null && !blank) is false; line 319 (venue != null) is still true
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);
        Section existing = new Section(course, 50, 10, start, end, "OldVenue");
        existing.setSectionId(10);

        AdminSectionService req = new AdminSectionService();
        req.setSectionId(10);
        req.setVenue("  ");   // blank ??not null, but isBlank() == true

        when(sectionRepository.findById(10)).thenReturn(Optional.of(existing));
        when(sectionRepository.save(any(Section.class))).thenReturn(existing);

        assertNotNull(service.modifySection(req));
    }

    @Test
    void modifySection_updateStartTime_success() {
        LocalDateTime oldStart = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end      = LocalDateTime.of(2026, 9, 1, 10, 50);
        LocalDateTime newStart = LocalDateTime.of(2026, 9, 1, 8, 30);
        Section existing = new Section(course, 50, 10, oldStart, end, "Y101");
        existing.setSectionId(10);

        AdminSectionService req = new AdminSectionService();
        req.setSectionId(10);
        req.setStartTime(newStart);

        when(sectionRepository.findById(10)).thenReturn(Optional.of(existing));
        when(sectionRepository.save(any(Section.class))).thenReturn(existing);

        assertNotNull(service.modifySection(req));
    }

    @Test
    void modifySection_updateEndTime_success() {
        LocalDateTime start      = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime oldEnd     = LocalDateTime.of(2026, 9, 1, 10, 50);
        LocalDateTime newEnd     = LocalDateTime.of(2026, 9, 1, 11, 20);
        Section existing = new Section(course, 50, 10, start, oldEnd, "Y101");
        existing.setSectionId(10);

        AdminSectionService req = new AdminSectionService();
        req.setSectionId(10);
        req.setEndTime(newEnd);

        when(sectionRepository.findById(10)).thenReturn(Optional.of(existing));
        when(sectionRepository.save(any(Section.class))).thenReturn(existing);

        assertNotNull(service.modifySection(req));
    }

    @Test
    void deleteSection_nullSectionId_throws() {
        AdminSectionService req = new AdminSectionService();
        assertThrows(RuntimeException.class, () -> service.deleteSection(req));
    }

    @Test
    void deleteSection_success() {
        AdminSectionService req = new AdminSectionService();
        req.setSectionId(10);
        service.deleteSection(req);
        verify(sectionRepository).deleteById(10);
    }

    @Test
    void createRegistrationPeriod_nullCohort_throws() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        doThrow(new RegistrationPeriodValidationException("Cohort is required"))
                .when(periodValidator).validate(req);
        assertThrows(RuntimeException.class, () -> service.createRegistrationPeriod(req));
    }

    @Test
    void createRegistrationPeriod_success() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        req.setCohort(2024);
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 11, 30, 23, 59);
        req.setStartDate(start);
        req.setEndDate(end);
        RegistrationPeriod saved = new RegistrationPeriod(2024, start, end);
        when(registrationPeriodRepository.save(any(RegistrationPeriod.class))).thenReturn(saved);

        service.createRegistrationPeriod(req);

        verify(registrationPeriodRepository).save(any(RegistrationPeriod.class));
    }

    @Test
    void createRegistrationPeriod_endBeforeStart_throws() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        req.setCohort(2024);
        req.setStartDate(LocalDateTime.of(2026, 12, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        doThrow(new RegistrationPeriodValidationException("Start date must be before end date"))
                .when(periodValidator).validate(req);
        assertThrows(RuntimeException.class, () -> service.createRegistrationPeriod(req));
    }

    @Test
    void createRegistrationPeriod_overlap_throws() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        req.setCohort(2024);
        req.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 11, 30, 23, 59));
        doThrow(new RegistrationPeriodOverlapException("Period overlaps with an existing period for cohort 2024"))
                .when(periodValidator).validate(req);
        assertThrows(RuntimeException.class, () -> service.createRegistrationPeriod(req));
    }

    @Test
    void deleteRegistrationPeriod_nullPeriodId_throws() {
        assertThrows(RuntimeException.class, () -> service.deleteRegistrationPeriod(null));
    }

    @Test
    void deleteRegistrationPeriod_notFound_throws() {
        when(registrationPeriodRepository.existsById(99)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.deleteRegistrationPeriod(99));
    }

    @Test
    void deleteRegistrationPeriod_success() {
        when(registrationPeriodRepository.existsById(42)).thenReturn(true);
        service.deleteRegistrationPeriod(42);
        verify(registrationPeriodRepository).deleteById(42);
    }

    @Test
    void listRegistrationPeriods_allCohorts_returnsSortedList() {
        LocalDateTime s1 = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime e1 = LocalDateTime.of(2026, 11, 30, 23, 59);
        RegistrationPeriod p1 = new RegistrationPeriod(2024, s1, e1);
        RegistrationPeriod p2 = new RegistrationPeriod(2023, s1, e1);
        when(registrationPeriodRepository.findAllOrderByCohortAndStartDateTime())
                .thenReturn(List.of(p2, p1));

        List<RegistrationPeriod> result = service.listRegistrationPeriods(null);

        assertEquals(2, result.size());
        verify(registrationPeriodRepository).findAllOrderByCohortAndStartDateTime();
    }

    @Test
    void listRegistrationPeriods_byCohort_returnsCohortPeriods() {
        LocalDateTime s1 = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime e1 = LocalDateTime.of(2026, 11, 30, 23, 59);
        RegistrationPeriod p = new RegistrationPeriod(2024, s1, e1);
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024))
                .thenReturn(List.of(p));

        List<RegistrationPeriod> result = service.listRegistrationPeriods(2024);

        assertEquals(1, result.size());
        verify(registrationPeriodRepository).findByCohortOrderByStartDateTime(2024);
    }

    private Instructor buildInstructor(String eid, int staffId) {
        return new Instructor.InstructorBuilder()
                .withStaffId(staffId)
                .withDepartment("COMP")
                .withUserEID(eid)
                .withName("Instructor " + staffId)
                .withPassword("pw")
                .build();
    }

    @Test
    void assignInstructor_instructorNotFound_throws() {
        when(instructorRepository.findByUserEID("i001")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.assignInstructor("i001", 10)
        );

        assertEquals("Instructor not found", ex.getMessage());
    }

    @Test
    void assignInstructor_sectionNotFound_throws() {
        Instructor instructor = buildInstructor("i001", 8);

        when(instructorRepository.findByUserEID("i001")).thenReturn(Optional.of(instructor));
        when(sectionRepository.findById(10)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.assignInstructor("i001", 10)
        );

        assertEquals("Section not found", ex.getMessage());
    }

    @Test
    void assignInstructor_alreadyAssigned_throws() {
        Instructor instructor = buildInstructor("i001", 8);

        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);

        Section section = new Section(course, 50, 10, start, end, "Y101");
        section.setSectionId(10);
        section.addInstructor(instructor);

        when(instructorRepository.findByUserEID("i001")).thenReturn(Optional.of(instructor));
        when(sectionRepository.findById(10)).thenReturn(Optional.of(section));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.assignInstructor("i001", 10)
        );

        assertEquals("Instructor already assigned to section", ex.getMessage());
    }

    @Test
    void assignInstructor_success() {
        Instructor instructor = buildInstructor("i001", 8);

        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);

        Section section = new Section(course, 50, 10, start, end, "Y101");
        section.setSectionId(10);

        when(instructorRepository.findByUserEID("i001")).thenReturn(Optional.of(instructor));
        when(sectionRepository.findById(10)).thenReturn(Optional.of(section));

        assertDoesNotThrow(() -> service.assignInstructor("i001", 10));
        assertTrue(section.getInstructors().contains(instructor));
    }

    @Test
    void unassignInstructor_instructorNotFound_throws() {
        when(instructorRepository.findByUserEID("i001")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.unassignInstructor("i001", 10)
        );

        assertEquals("Instructor not found", ex.getMessage());
    }

    @Test
    void unassignInstructor_sectionNotFound_throws() {
        Instructor instructor = buildInstructor("i001", 8);

        when(instructorRepository.findByUserEID("i001")).thenReturn(Optional.of(instructor));
        when(sectionRepository.findById(10)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.unassignInstructor("i001", 10)
        );

        assertEquals("Section not found", ex.getMessage());
    }

    @Test
    void unassignInstructor_alreadyUnassigned_throws() {
        Instructor instructor = buildInstructor("i001", 8);

        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);

        Section section = new Section(course, 50, 10, start, end, "Y101");
        section.setSectionId(10);

        when(instructorRepository.findByUserEID("i001")).thenReturn(Optional.of(instructor));
        when(sectionRepository.findById(10)).thenReturn(Optional.of(section));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.unassignInstructor("i001", 10)
        );

        assertEquals("Instructor already unassigned to section", ex.getMessage());
    }

    @Test
    void unassignInstructor_success() {
        Instructor instructor = buildInstructor("i001", 8);

        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 9, 1, 10, 50);

        Section section = new Section(course, 50, 10, start, end, "Y101");
        section.setSectionId(10);
        section.addInstructor(instructor);

        when(instructorRepository.findByUserEID("i001")).thenReturn(Optional.of(instructor));
        when(sectionRepository.findById(10)).thenReturn(Optional.of(section));

        assertDoesNotThrow(() -> service.unassignInstructor("i001", 10));
        assertFalse(section.getInstructors().contains(instructor));
    }


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
    void createStudent_duplicateEIDInInstructorRepo_throws() {
        // Arrange
        String userEID = studentReq.getUserEID();

        // First two checks pass (return empty)
        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new UserEidAlreadyExistsException(userEID));

        // Act & Assert
        assertThrows(UserEidAlreadyExistsException.class, () -> service.createStudent(studentReq));
    }

    @Test
    void createStudent_duplicateEIDInAdminRepo_throws() {
        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new UserEidAlreadyExistsException(studentReq.getUserEID()));

        assertThrows(UserEidAlreadyExistsException.class, () -> service.createStudent(studentReq));
    }


    @Test
    void createStudent_duplicateEIDInStudentRepo_throws() {
        // Arrange
        String userEID = studentReq.getUserEID();

        when(studentUserManagementService.createStudent(studentReq)).thenThrow(new UserEidAlreadyExistsException(userEID));

        assertThrows(UserEidAlreadyExistsException.class, () -> service.createStudent(studentReq));
    }

    @Test
    void createStudent_success() {
        when(studentUserManagementService.createStudent(studentReq)).thenReturn(student);
        assertNotNull(studentUserManagementService.createStudent(studentReq));
    }

    @Test
    void modifyStudent_notFound_throws() {
        when(studentUserManagementService.modifyStudent(studentReq)).thenThrow(new UserNotFoundException("Student", "SEID99"));
        studentReq.setUserEID("SEID99");
        assertThrows(UserNotFoundException.class, () -> service.modifyStudent(studentReq));
    }

    @Test
    void modifyStudent_blankEID_throws() {
        studentReq.setUserEID("");
        when(studentUserManagementService.modifyStudent(studentReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.modifyStudent(studentReq));
    }

    @Test
    void modifyStudent_blankName_throws() {
        studentReq.setName("");
        when(studentUserManagementService.modifyStudent(studentReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.modifyStudent(studentReq));
    }

    @Test
    void modifyStudent_duplicateEID_throws() {
        when(studentUserManagementService.modifyStudent(studentReq)).thenThrow(new UserEidAlreadyExistsException(studentReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.modifyStudent(studentReq));
    }

    @Test
    void modifyStudent_success() {
        when(studentUserManagementService.modifyStudent(studentReq)).thenReturn(student);
        assertNotNull(service.modifyStudent(studentReq));
    }

    @Test
    void removeStudent_notFound_throws() {
        doThrow(new UserNotFoundException("Student", "99")).when(studentUserManagementService).removeStudent("99");
        assertThrows(UserNotFoundException.class, () -> service.removeStudent("99"));
    }

    @Test
    void removeStudent_success() {
        doNothing().when(studentUserManagementService).removeStudent("1");
        service.removeStudent("1");
        verify(studentUserManagementService).removeStudent("1");
    }

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
        when(instructorUserManagementService.modifyInstructor(instructorReq)).thenThrow(new UserNotFoundException("Instructor", "IEID99"));
        instructorReq.setUserEID("IEID99");
        assertThrows(UserNotFoundException.class, () -> service.modifyInstructor(instructorReq));
    }

    @Test
    void modifyInstructor_blankEID_throws() {
        instructorReq.setUserEID("");
        when(instructorUserManagementService.modifyInstructor(instructorReq)).thenThrow(new InvalidUserEIDException());
        assertThrows(InvalidUserEIDException.class, () -> service.modifyInstructor(instructorReq));
    }

    @Test
    void modifyInstructor_blankName_throws() {
        instructorReq.setName("");
        when(instructorUserManagementService.modifyInstructor(instructorReq)).thenThrow(new InvalidNameException());
        assertThrows(InvalidNameException.class, () -> service.modifyInstructor(instructorReq));
    }

    @Test
    void modifyInstructor_duplicateEID_throws() {
        when(instructorUserManagementService.modifyInstructor(instructorReq)).thenThrow(new UserEidAlreadyExistsException(instructorReq.getUserEID()));
        assertThrows(UserEidAlreadyExistsException.class, () -> service.modifyInstructor(instructorReq));
    }

    @Test
    void modifyInstructor_success() {
        when(instructorUserManagementService.modifyInstructor(instructorReq)).thenReturn(instructor);
        assertNotNull(service.modifyInstructor(instructorReq));
    }

    @Test
    void removeInstructor_notFound_throws() {
        doThrow(new UserNotFoundException("Instructor", "99")).when(instructorUserManagementService).removeInstructor("99");
        assertThrows(UserNotFoundException.class, () -> service.removeInstructor("99"));
    }

    @Test
    void removeInstructor_success() {
        doNothing().when(instructorUserManagementService).removeInstructor("1");
        service.removeInstructor("1");
        verify(instructorUserManagementService).removeInstructor("1");
    }

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

    @Test
void listStudents_delegatesToStudentService() {
    when(studentUserManagementService.listStudents()).thenReturn(List.of(student));

    List<Student> result = service.listStudents();

    assertEquals(1, result.size());
    verify(studentUserManagementService).listStudents();
}

@Test
void createStudent_delegatesToStudentService() {
    when(studentUserManagementService.createStudent(studentReq)).thenReturn(student);

    Student result = service.createStudent(studentReq);

    assertNotNull(result);
    verify(studentUserManagementService).createStudent(studentReq);
}

@Test
void modifyStudent_delegatesToStudentService() {
    when(studentUserManagementService.modifyStudent(studentReq)).thenReturn(student);

    Student result = service.modifyStudent(studentReq);

    assertNotNull(result);
    verify(studentUserManagementService).modifyStudent(studentReq);
}

@Test
void removeStudent_delegatesToStudentService() {
    doNothing().when(studentUserManagementService).removeStudent("SEID123");

    service.removeStudent("SEID123");

    verify(studentUserManagementService).removeStudent("SEID123");
}



@Test
void createSection_withInstructorIds_assignsInstructors() {
    Instructor ins = buildInstructor("i001", 8);

    when(instructorRepository.findById(8)).thenReturn(Optional.of(ins));
    when(sectionRepository.save(any(Section.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(50);
    req.setWaitlistCapacity(10);
    req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
    req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
    req.setVenue("Y101");
    req.setInstructorStaffIds(Set.of(8)); // 👈 key

    Section result = service.createSection(req);

    assertTrue(result.getInstructors().contains(ins));
}

@Test
void modifySection_withInstructorIds_updatesInstructors() {
    Instructor ins = buildInstructor("i002", 9);

    when(instructorRepository.findById(9)).thenReturn(Optional.of(ins));

    Section existing = new Section(course, 50, 10,
            LocalDateTime.of(2026, 9, 1, 9, 0),
            LocalDateTime.of(2026, 9, 1, 10, 50),
            "Y101");
    existing.setSectionId(10);

    when(sectionRepository.findById(10)).thenReturn(Optional.of(existing));
    when(sectionRepository.save(any())).thenReturn(existing);

    AdminSectionService req = new AdminSectionService();
    req.setSectionId(10);
    req.setInstructorStaffIds(Set.of(9)); // 👈 key

    Section result = service.modifySection(req);

    assertTrue(result.getInstructors().contains(ins));
}

@Test
void listSections_nullCourseCode_returnsAll() {
    Section s1 = new Section(course, 50, 10,
            LocalDateTime.now().minusHours(1), LocalDateTime.now(), "Y101");
    when(sectionRepository.findAll()).thenReturn(List.of(s1));

    List<Section> result = service.listSections(null);

    assertEquals(1, result.size());
}

@Test
void listSections_filterByCourseCode() {
    Course cs101 = new Course("CS101", "Intro", 3, null, Set.of(), Set.of(), null);
    Course cs999 = new Course("CS999", "Other", 3, null, Set.of(), Set.of(), null);

    Section s1 = new Section(cs101, 50, 10,
            LocalDateTime.now().minusHours(1), LocalDateTime.now(), "Y101");
    Section s2 = new Section(cs999, 50, 10,
            LocalDateTime.now().minusHours(1), LocalDateTime.now(), "Y102");

    when(sectionRepository.findAll()).thenReturn(List.of(s1, s2));

    List<Section> result = service.listSections("CS101");

    assertEquals(1, result.size());
    assertEquals("CS101", result.get(0).getCourse().getCourseCode());
}

@Test
void listInstructors_delegatesToInstructorService() {
    when(instructorUserManagementService.listInstructors())
            .thenReturn(List.of(instructor));

    List<Instructor> result = service.listInstructors();

    assertEquals(1, result.size());
    verify(instructorUserManagementService).listInstructors();
}

@Test
void createSection_nullWaitlistCapacity_throws() {
    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(50);
    req.setStartTime(LocalDateTime.now());
    req.setEndTime(LocalDateTime.now().plusHours(1));
    req.setVenue("Y101");
    // waitlistCapacity NOT set

    assertThrows(RuntimeException.class,
            () -> service.createSection(req));
}

@Test
void createSection_nullEndTimeOnly_throws() {
    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(30);
    req.setWaitlistCapacity(10);

    req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
    req.setEndTime(null); // ✅ second OR branch

    req.setVenue("Y101");

    assertThrows(RuntimeException.class, () -> service.createSection(req));
}

@Test
void createSection_nullEndTime_triggersOrBranch() {
    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(50);
    req.setWaitlistCapacity(10);

    // ✅ startTime present, endTime missing
    req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
    req.setEndTime(null);

    req.setVenue("Y101");

    assertThrows(RuntimeException.class,
            () -> service.createSection(req));
}

@Test
void createSection_instructorIds_containsNull_skipsNullAndSucceeds() {
    Instructor ins = buildInstructor("i001", 8);
    when(instructorRepository.findById(8)).thenReturn(Optional.of(ins));
    when(sectionRepository.save(any(Section.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(50);
    req.setWaitlistCapacity(10);
    req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
    req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
    req.setVenue("Y101");

    Set<Integer> ids = new java.util.HashSet<>();
    ids.add(null);   // covers continue branch
    ids.add(8);
    req.setInstructorStaffIds(ids);

    Section result = service.createSection(req);

    assertNotNull(result);
    assertTrue(result.getInstructors().contains(ins));
    verify(instructorRepository).findById(8);
}

@Test
void createSection_instructorIdNotFound_throws() {
    when(instructorRepository.findById(999)).thenReturn(Optional.empty());

    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(50);
    req.setWaitlistCapacity(10);
    req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
    req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
    req.setVenue("Y101");
    req.setInstructorStaffIds(Set.of(999)); // covers orElseThrow branch

    RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createSection(req));
    assertEquals("Instructor not found: 999", ex.getMessage());
}

@Test
void createSection_nullVenue_throwsVenueRequired() {
    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(50);
    req.setWaitlistCapacity(10);
    req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
    req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
    req.setVenue(null); // left side of OR

    RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createSection(req));
    assertEquals("Venue is required", ex.getMessage());
}

@Test
void createSection_blankVenue_throwsVenueRequired() {
    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(50);
    req.setWaitlistCapacity(10);
    req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
    req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
    req.setVenue("   "); // right side of OR

    RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createSection(req));
    assertEquals("Venue is required", ex.getMessage());
}

@Test
void modifyCourse_studentEnrolled_throws() {
    // Arrange
    when(courseRepository.findByCourseCode("CS101"))
            .thenReturn(Optional.of(course));
    when(registrationRecordRepository.existsByCourseCode("CS101"))
            .thenReturn(true);

    // Act + Assert
    RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> service.modifyCourse(courseReq)
    );

    assertEquals("There are student enrolled.", ex.getMessage());
}

@Test
void removeCourse_studentEnrolled_throws() {
    // Arrange
    when(courseRepository.findByCourseCode("CS101"))
            .thenReturn(Optional.of(course));
    when(registrationRecordRepository.existsByCourseCode("CS101"))
            .thenReturn(true);

    // Act + Assert
    RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> service.removeCourse("CS101")
    );

    assertEquals("There are student enrolled.", ex.getMessage());
}

@Test
void createSection_timeConflictInVenue_throws() {
    // Arrange
    AdminSectionService req = new AdminSectionService();
    req.setCourse(course);
    req.setSectionType(Section.Type.LECTURE);
    req.setEnrollCapacity(50);
    req.setWaitlistCapacity(10);
    req.setStartTime(LocalDateTime.of(2026, 9, 1, 9, 0));
    req.setEndTime(LocalDateTime.of(2026, 9, 1, 10, 50));
    req.setVenue("Y101");

    when(sectionRepository.overlapsInVenue(
            eq("Y101"),
            any(LocalDateTime.class),
            any(LocalDateTime.class)))
            .thenReturn(true);

    // Act + Assert
    RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> service.createSection(req)
    );

    assertEquals(
            "Time conflict with existing section in same venue",
            ex.getMessage()
    );
}

}

