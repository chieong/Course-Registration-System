package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminSectionService;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
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
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodOverlapException;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodValidationException;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodValidator;
import java.time.LocalDateTime;
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

    @Mock private AdminRepositoryPort adminRepository;
    @Mock private CourseRepositoryPort courseRepository;
    @Mock private SectionRepositoryPort sectionRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RegistrationPeriodValidator periodValidator;
    @InjectMocks private AdministrativeService service;

    private AdminUserRequest userReq;
    private AdminCourseRequest courseReq;
    private Admin admin;
    private Course course;
    private Course prereqCourse;

    @BeforeEach
    void setUp() {
        userReq = new AdminUserRequest();
        userReq.setUserEID("EID123");
        userReq.setName("Test Admin");
        userReq.setPassword("pass123");

        courseReq = new AdminCourseRequest();
        courseReq.setCourseCode("CS101");
        courseReq.setTitle("Intro CS");
        courseReq.setCredits(3);

        admin = new Admin.AdminBuilder().withStaffId(1).withUserEID("EID123").withName("Test").withPassword("enc").build();
        course = new Course("CS101", "Intro CS", 3, null, null, Set.of(), Set.of(), null);
        prereqCourse = new Course("CS102", "Prereq CS", 3, null, null, Set.of(), Set.of(), null);
    }

    @Test
    void listUsers_success() {
        when(adminRepository.findAll()).thenReturn(List.of(admin));
        assertEquals(1, service.listUsers().size());
    }

    @Test
    void createUser_blankEID_throws() {
        userReq.setUserEID("");
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_blankName_throws() {
        userReq.setName("");
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_blankPassword_throws() {
        userReq.setPassword("");
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_duplicateEID_throws() {
        when(adminRepository.findByUserEID(anyString())).thenReturn(Optional.of(admin));
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_success() {
        when(adminRepository.findByUserEID(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(adminRepository.save(any())).thenReturn(admin);
        assertNotNull(service.createUser(userReq));
    }

    @Test
    void modifyUser_notFound_throws() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.modifyUser(99, userReq));
    }

    @Test
    void modifyUser_blankEID_throws() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        userReq.setUserEID("");
        assertThrows(RuntimeException.class, () -> service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_blankName_throws() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        userReq.setName("");
        assertThrows(RuntimeException.class, () -> service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_duplicateEID_throws() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        when(adminRepository.findByUserEID(anyString())).thenReturn(Optional.of(new Admin.AdminBuilder().withStaffId(2).build()));
        assertThrows(RuntimeException.class, () -> service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_keepOldPassword_success() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        when(adminRepository.findByUserEID(anyString())).thenReturn(Optional.of(admin));
        userReq.setPassword("");
        when(adminRepository.save(any())).thenReturn(admin);
        assertNotNull(service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_success() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        when(adminRepository.findByUserEID(anyString())).thenReturn(Optional.of(admin));
        when(adminRepository.save(any())).thenReturn(admin);
        assertNotNull(service.modifyUser(1, userReq));
    }

    @Test
    void removeUser_notFound_throws() {
        when(adminRepository.existsById(anyInt())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.removeUser(99));
    }

    @Test
    void removeUser_success() {
        when(adminRepository.existsById(anyInt())).thenReturn(true);
        service.removeUser(1);
        verify(adminRepository).deleteById(1);
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
        Course cs101 = new Course("CS101", "Old Title", 3, null, null, Set.of(), Set.of(), null);
        when(courseRepository.findByCourseCode("CS999")).thenReturn(Optional.of(cs101));
        when(courseRepository.existsByCourseCode("CS999")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.modifyCourse(courseReq));
    }

    @Test
    void modifyCourse_renameToNewCode_success() {
        // newCourseCode differs from existingCourse.getCourseCode() and does NOT already exist
        courseReq.setCourseCode("CS999");
        courseReq.setTitle(null);   // also covers title==null branch
        Course cs101 = new Course("CS101", "Old Title", 3, null, null, Set.of(), Set.of(), null);
        when(courseRepository.findByCourseCode("CS999")).thenReturn(Optional.of(cs101));
        when(courseRepository.existsByCourseCode("CS999")).thenReturn(false);
        when(courseRepository.save(any())).thenReturn(cs101);
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

    // ── createUser – null-input guards ───────────────────────────────────────

    @Test
    void createUser_nullEID_throws() {
        userReq.setUserEID(null);
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_nullName_throws() {
        userReq.setName(null);
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    @Test
    void createUser_nullPassword_throws() {
        userReq.setPassword(null);
        assertThrows(RuntimeException.class, () -> service.createUser(userReq));
    }

    // ── modifyUser – remaining branches ──────────────────────────────────────

    @Test
    void modifyUser_nullEID_throws() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        userReq.setUserEID(null);
        assertThrows(RuntimeException.class, () -> service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_nullName_throws() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        userReq.setName(null);
        assertThrows(RuntimeException.class, () -> service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_newEIDNotTaken_success() {
        // EID is changing to one that does not exist in the system yet
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        when(adminRepository.findByUserEID(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(adminRepository.save(any())).thenReturn(admin);
        assertNotNull(service.modifyUser(1, userReq));
    }

    @Test
    void modifyUser_nullPassword_keepsExistingPassword() {
        // Null password must not re-encode; existing encoded password is preserved
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        when(adminRepository.findByUserEID(anyString())).thenReturn(Optional.of(admin));
        userReq.setPassword(null);
        when(adminRepository.save(any())).thenReturn(admin);
        Admin result = service.modifyUser(1, userReq);
        assertNotNull(result);
        // passwordEncoder.encode must NOT have been called
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ── createCourse – additional validation paths ────────────────────────────

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

    // ── modifyCourse – resolveCourseCodes failure paths ───────────────────────

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

    // ── removeCourse – null code ──────────────────────────────────────────────

    @Test
    void removeCourse_nullCode_throws() {
        assertThrows(RuntimeException.class, () -> service.removeCourse(null));
    }

    // ── createSection ─────────────────────────────────────────────────────────

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

    // ── modifySection ─────────────────────────────────────────────────────────

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
        Course newCourse = new Course("CS200", "Algorithms", 3, null, "2026A", Set.of(), Set.of(), Set.of());

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
        req.setVenue("  ");   // blank – not null, but isBlank() == true

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

    // ── deleteSection ─────────────────────────────────────────────────────────

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

    // ── createRegistrationPeriod ──────────────────────────────────────────────

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
        req.setTerm("2026A");
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 11, 30, 23, 59);
        req.setStartDate(start);
        req.setEndDate(end);
        RegistrationPeriod saved = new RegistrationPeriod(2024, start, end, "2026A");
        when(registrationPeriodRepository.save(any(RegistrationPeriod.class))).thenReturn(saved);

        service.createRegistrationPeriod(req);

        verify(registrationPeriodRepository).save(any(RegistrationPeriod.class));
    }

    @Test
    void createRegistrationPeriod_endBeforeStart_throws() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        req.setCohort(2024);
        req.setTerm("2026A");
        req.setStartDate(LocalDateTime.of(2026, 12, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        doThrow(new RegistrationPeriodValidationException("Start date must be before end date"))
                .when(periodValidator).validate(req);
        assertThrows(RuntimeException.class, () -> service.createRegistrationPeriod(req));
    }

    @Test
    void createRegistrationPeriod_blankTerm_throws() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        req.setCohort(2024);
        req.setTerm("");
        doThrow(new RegistrationPeriodValidationException("Term is required"))
                .when(periodValidator).validate(req);
        assertThrows(RuntimeException.class, () -> service.createRegistrationPeriod(req));
    }

    @Test
    void createRegistrationPeriod_overlap_throws() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        req.setCohort(2024);
        req.setTerm("2026A");
        req.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 11, 30, 23, 59));
        doThrow(new RegistrationPeriodOverlapException("Period overlaps with an existing period for cohort 2024"))
                .when(periodValidator).validate(req);
        assertThrows(RuntimeException.class, () -> service.createRegistrationPeriod(req));
    }

    // ── deleteRegistrationPeriod ──────────────────────────────────────────────

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

    // ── listRegistrationPeriods ───────────────────────────────────────────────

    @Test
    void listRegistrationPeriods_allCohorts_returnsSortedList() {
        LocalDateTime s1 = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime e1 = LocalDateTime.of(2026, 11, 30, 23, 59);
        RegistrationPeriod p1 = new RegistrationPeriod(2024, s1, e1, "2026A");
        RegistrationPeriod p2 = new RegistrationPeriod(2023, s1, e1, "2026A");
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
        RegistrationPeriod p = new RegistrationPeriod(2024, s1, e1, "2026A");
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024))
                .thenReturn(List.of(p));

        List<RegistrationPeriod> result = service.listRegistrationPeriods(2024);

        assertEquals(1, result.size());
        verify(registrationPeriodRepository).findByCohortOrderByStartDateTime(2024);
    }

    @Mock private InstructorRepository instructorRepository;

    private Instructor buildInstructor(String eid, int staffId) {
        return (Instructor) new Instructor.InstructorBuilder()
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

}