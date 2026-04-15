package org.cityuhk.CourseRegistrationSystem.Service;

import org.cityuhk.CourseRegistrationSystem.Controller.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.Controller.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.CourseRepository;
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

    @Mock private AdminRepository adminRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AdministrativeService service;

    private AdminUserRequest userReq;
    private AdminCourseRequest courseReq;
    private Admin admin;
    private Course course;

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
        verify(adminRepository).save(any());
    }

    @Test
    void modifyUser_notFound_throws() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.modifyUser(99, userReq));
    }

    @Test
    void modifyUser_duplicateEID_throws() {
        when(adminRepository.findById(anyInt())).thenReturn(Optional.of(admin));
        when(adminRepository.findByUserEID(anyString())).thenReturn(Optional.of(new Admin.AdminBuilder().withStaffId(2).build()));
        assertThrows(RuntimeException.class, () -> service.modifyUser(1, userReq));
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
    void createCourse_success() {
        when(courseRepository.existsByCourseCode(anyString())).thenReturn(false);
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.createCourse(courseReq));
    }

    @Test
    void modifyCourse_notFound_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.modifyCourse("CS101", courseReq));
    }

    @Test
    void modifyCourse_duplicateNewCode_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        when(courseRepository.existsByCourseCode(anyString())).thenReturn(true);
        courseReq.setCourseCode("CS999");
        assertThrows(RuntimeException.class, () -> service.modifyCourse("CS101", courseReq));
    }

    @Test
    void modifyCourse_selfPrereq_throws() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        courseReq.setPrerequisiteCourseCodes(Set.of("CS101"));
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        assertThrows(RuntimeException.class, () -> service.modifyCourse("CS101", courseReq));
    }

    @Test
    void modifyCourse_success() {
        when(courseRepository.findByCourseCode(anyString())).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenReturn(course);
        assertNotNull(service.modifyCourse("CS101", courseReq));
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
}