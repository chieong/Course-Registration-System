package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Controller.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.CourseRepository;
import org.cityuhk.CourseRegistrationSystem.Service.AdministrativeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministrativeServiceTest {

    @Mock private AdminRepository adminRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdministrativeService administrativeService;

    private Course makeCourse(String code, String title, int credits) {
        return new Course(
                code, title, credits,
                "old description", "old term",
                new HashSet<>(), new HashSet<>(), null
        );
    }

    @Test
    void modifyCourse_success_updateBasicFields_keepCourseCode() {
        Course existing = makeCourse("CS101", "Intro", 3);

        AdminCourseRequest request = new AdminCourseRequest();
        request.setCourseCode(" "); // blank => keep existing
        request.setTitle(" Data Structures ");
        request.setDescription("Updated description");
        request.setTerm("Semester A");
        request.setCredits(4);

        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(existing));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        Course result = administrativeService.modifyCourse("CS101", request);

        assertNotNull(result);
        assertEquals("CS101", result.getCourseCode());
        assertEquals("Data Structures", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertEquals("Semester A", result.getTerm());
        assertEquals(4, result.getCredits());

        verify(courseRepository).findByCourseCode("CS101");
        verify(courseRepository).save(existing);
        verify(courseRepository, never()).existsByCourseCode(anyString());
    }

    @Test
    void modifyCourseThrowWhenCourseCodeBlank() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> administrativeService.modifyCourse("   ", new AdminCourseRequest()));
        assertEquals("Course code is required", ex.getMessage());
    }

    @Test
    void modifyCourseThrowWhenCourseNotFound() {
        when(courseRepository.findByCourseCode("CS999")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> administrativeService.modifyCourse("CS999", new AdminCourseRequest()));

        assertEquals("Course not found", ex.getMessage());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void modifyCourseThrowWhenNewCodeAlreadyExists() {
        Course existing = makeCourse("CS101", "Intro", 3);
        AdminCourseRequest request = new AdminCourseRequest();
        request.setCourseCode("CS201");

        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(existing));
        when(courseRepository.existsByCourseCode("CS201")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> administrativeService.modifyCourse("CS101", request));

        assertEquals("Course code already exists", ex.getMessage());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void modifyCourseThrowWhenTitleBlank() {
        Course existing = makeCourse("CS101", "Intro", 3);
        AdminCourseRequest request = new AdminCourseRequest();
        request.setTitle("   ");

        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> administrativeService.modifyCourse("CS101", request));

        assertEquals("Course title cannot be blank", ex.getMessage());
    }

    @Test
    void modifyCourseThrowWhenCreditsNegative() {
        Course existing = makeCourse("CS101", "Intro", 3);
        AdminCourseRequest request = new AdminCourseRequest();
        request.setCredits(-1);

        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> administrativeService.modifyCourse("CS101", request));

        assertEquals("Credits cannot be negative", ex.getMessage());
    }

    @Test
    void modifyCourseSuccess() {
        Course existing = makeCourse("CS101", "Intro", 3);
        Course prereq = makeCourse("CS100", "Foundation", 3);
        Course exclusive = makeCourse("CS102", "Other", 3);

        AdminCourseRequest request = new AdminCourseRequest();
        request.setPrerequisiteCourseCodes(Set.of("CS100"));
        request.setExclusiveCourseCodes(Set.of("CS102"));

        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(existing));
        when(courseRepository.findByCourseCode("CS100")).thenReturn(Optional.of(prereq));
        when(courseRepository.findByCourseCode("CS102")).thenReturn(Optional.of(exclusive));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        Course result = administrativeService.modifyCourse("CS101", request);

        assertTrue(result.getPrerequisiteCourses().contains(prereq));
        assertTrue(result.getExclusiveCourses().contains(exclusive));
        verify(courseRepository).save(existing);
    }

    @Test
void modifyCourse_throwLine_selfPrerequisite() {
    Course existing = makeCourse("CS101", "Intro", 3);

    AdminCourseRequest request = new AdminCourseRequest();
    request.setCourseCode("   "); // keep current code => newCourseCode = CS101
    request.setPrerequisiteCourseCodes(Set.of("CS101")); // self prerequisite

    // used for: loading existing course + resolving prerequisite CS101
    when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(existing));

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> administrativeService.modifyCourse("CS101", request));

    assertEquals("A course cannot be its own prerequisite", ex.getMessage());
    verify(courseRepository, never()).save(any(Course.class));
}

@Test
void modifyCourse_throwLine_selfExclusive() {
    Course existing = makeCourse("CS101", "Intro", 3);

    AdminCourseRequest request = new AdminCourseRequest();
    request.setCourseCode("   "); // keep current code => newCourseCode = CS101
    request.setExclusiveCourseCodes(Set.of("CS101")); // self exclusive

    // used for: loading existing course + resolving exclusive CS101
    when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(existing));

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> administrativeService.modifyCourse("CS101", request));

    assertEquals("A course cannot be its own exclusive course", ex.getMessage());
    verify(courseRepository, never()).save(any(Course.class));
}

    
}


