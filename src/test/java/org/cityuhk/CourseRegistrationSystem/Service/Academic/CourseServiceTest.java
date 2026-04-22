package org.cityuhk.CourseRegistrationSystem.Service.Academic;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.CourseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepositoryPort courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void getCourse_DelegatesToRepositoryByCourseCode() {
        Course expected = new Course("CS101", "Intro CS", 3, "Basics", Set.of(), Set.of(), Set.of());
        when(courseRepository.getCourseByCourseCode("CS101")).thenReturn(expected);

        Course found = courseService.getCourse("CS101");

        assertSame(expected, found);
        verify(courseRepository).getCourseByCourseCode("CS101");
        verifyNoMoreInteractions(courseRepository);
    }

    @Test
    void getCourse_WhenRepositoryHasNoMatch_ReturnsNull() {
        when(courseRepository.getCourseByCourseCode("MISSING")).thenReturn(null);

        Course found = courseService.getCourse("MISSING");

        assertNull(found);
        verify(courseRepository).getCourseByCourseCode("MISSING");
        verifyNoMoreInteractions(courseRepository);
    }

    @Test
    void getAllCourses_ReturnsRepositoryResultWithoutModification() {
        Course c1 = new Course("CS101", "Intro CS", 3, "Basics", Set.of(), Set.of(), Set.of());
        Course c2 = new Course("CS102", "Data Structures", 3, "Core", Set.of(), Set.of(), Set.of());
        List<Course> expected = List.of(c1, c2);
        when(courseRepository.findAll()).thenReturn(expected);

        List<Course> found = courseService.getAllCourses();

        assertEquals(2, found.size());
        assertSame(expected, found);
        assertSame(c1, found.get(0));
        assertSame(c2, found.get(1));
        verify(courseRepository).findAll();
        verifyNoMoreInteractions(courseRepository);
    }
}
