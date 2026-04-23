package org.cityuhk.CourseRegistrationSystem.Service.Academic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cityuhk.CourseRegistrationSystem.Model.*;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.*;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.studentListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class StudentListServiceTest {

    @Mock
    private InstructorRepositoryPort instructorRepository;

    @Mock
    private RegistrationRecordRepositoryPort registrationRecordRepository;

    @InjectMocks
    private StudentListService studentListService;

    private final String instructorEID = "inst123";

    @Test
    void testGetStudentList_Success() {
        // 1. Setup Mock Data
        Instructor mockInstructor = mock(Instructor.class);
        Section section1 = mock(Section.class);
        when(section1.getSectionId()).thenReturn(101);

        Set<Section> sections = new HashSet<>(Collections.singletonList(section1));

        RegistrationRecord record = mock(RegistrationRecord.class);
        when(record.getStudent()).thenReturn(mock(Student.class));
        when(record.getCourse()).thenReturn(mock(Course.class));
        when(record.getSection()).thenReturn(section1);

        // 2. Define Mock Behavior
        when(instructorRepository.findByUserEID(instructorEID)).thenReturn(Optional.of(mockInstructor));
        when(mockInstructor.getSections()).thenReturn(sections);
        when(registrationRecordRepository.findBySectionId(101)).thenReturn(List.of(record));

        // 3. Execute
        List<studentListResponse> result = studentListService.getStudentListByInstructorEID(instructorEID);

        // 4. Verify logic
        assertEquals(1, result.size());
        verify(registrationRecordRepository, times(1)).findBySectionId(101);
    }

    @Test
    void testGetStudentList_InstructorNotFound_ThrowsException() {
        when(instructorRepository.findByUserEID(instructorEID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentListService.getStudentListByInstructorEID(instructorEID);
        });

        assertEquals("Instructor not found", exception.getMessage());
    }

    @Test
    void testGetStudentList_NoSections_ThrowsException() {
        Instructor mockInstructor = mock(Instructor.class);
        when(instructorRepository.findByUserEID(instructorEID)).thenReturn(Optional.of(mockInstructor));
        when(mockInstructor.getSections()).thenReturn(Collections.emptySet());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentListService.getStudentListByInstructorEID(instructorEID);
        });

        assertEquals("Section not found", exception.getMessage());
    }

    @Test
    void testGetStudentList_MultipleSections_AggregatesResults() {
        // Testing the logical loop for multiple sections
        Instructor mockInstructor = mock(Instructor.class);
        Section s1 = mock(Section.class);
        Section s2 = mock(Section.class);

        when(s1.getSectionId()).thenReturn(1);
        when(s2.getSectionId()).thenReturn(2);
        when(instructorRepository.findByUserEID(instructorEID)).thenReturn(Optional.of(mockInstructor));
        when(mockInstructor.getSections()).thenReturn(Set.of(s1, s2));

        // Mocking records for each section
        when(registrationRecordRepository.findBySectionId(1)).thenReturn(List.of(new RegistrationRecord()));
        when(registrationRecordRepository.findBySectionId(2)).thenReturn(List.of(new RegistrationRecord()));

        List<studentListResponse> result = studentListService.getStudentListByInstructorEID(instructorEID);

        // Verify that the list combines records from BOTH sections
        assertEquals(2, result.size());
    }
}