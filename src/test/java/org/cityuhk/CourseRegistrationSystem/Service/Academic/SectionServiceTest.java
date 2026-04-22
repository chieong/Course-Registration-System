package org.cityuhk.CourseRegistrationSystem.Service.Academic;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.CourseRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepositoryPort sectionRepository;

    @Mock
    private CourseRepositoryPort courseRepository;

    @InjectMocks
    private SectionService sectionService;

    @Test
    void getAllSections_ReturnsRepositoryListInSameOrder() {
        Course c1 = new Course("CS101", "Intro CS", 3, "Basics", "2026A", Set.of(), Set.of(), Set.of());
        Course c2 = new Course("CS102", "Data Structures", 3, "Core", "2026A", Set.of(), Set.of(), Set.of());
        Section s1 = new Section(c1, 50, 10, LocalDateTime.of(2026, 9, 1, 9, 0), LocalDateTime.of(2026, 9, 1, 10, 50), "Y123");
        Section s2 = new Section(c2, 40, 8, LocalDateTime.of(2026, 9, 2, 11, 0), LocalDateTime.of(2026, 9, 2, 12, 50), "Y456");
        List<Section> expected = List.of(s1, s2);
        when(sectionRepository.findAll()).thenReturn(expected);

        List<Section> found = sectionService.getAllSections();

        assertSame(expected, found);
        assertSame(s1, found.get(0));
        assertSame(s2, found.get(1));
        verify(sectionRepository).findAll();
        verifyNoMoreInteractions(sectionRepository);
        verifyNoInteractions(courseRepository);
    }

    @Test
    void getAllSections_WhenNoSectionExists_ReturnsEmptyList() {
        List<Section> expected = Collections.emptyList();
        when(sectionRepository.findAll()).thenReturn(expected);

        List<Section> found = sectionService.getAllSections();

        assertSame(expected, found);
        verify(sectionRepository).findAll();
        verifyNoMoreInteractions(sectionRepository);
        verifyNoInteractions(courseRepository);
    }
}
