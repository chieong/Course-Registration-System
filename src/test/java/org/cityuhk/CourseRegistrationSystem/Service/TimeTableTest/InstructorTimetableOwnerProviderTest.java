package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.InstructorTimetableOwnerProvider;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidationException;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("InstructorTimetableOwnerProvider Tests")
class InstructorTimetableOwnerProviderTest {

    private InstructorTimetableOwnerProvider provider;
    private TimetableValidator mockValidator;
    private InstructorRepository mockRepository;

    @BeforeEach
    void setUp() {
        mockValidator = mock(TimetableValidator.class);
        mockRepository = mock(InstructorRepository.class);
        provider = new InstructorTimetableOwnerProvider(mockValidator, mockRepository);
    }

    @Test
    @DisplayName("Should return Instructor user type")
    void testUserType() {
        assertEquals(TimetableData.UserType.Instructor, provider.userType());
    }

    @Test
    @DisplayName("Should return correct owner ID label")
    void testOwnerIdLabel() {
        assertEquals("Staff ID", provider.ownerIdLabel());
    }

    @Test
    @DisplayName("Should validate instructor for export successfully")
    void testValidateForExportSuccess() throws TimetableValidationException {
        assertDoesNotThrow(() -> provider.validateForExport(456));
        verify(mockValidator, times(1)).validateInstructorForExport(456);
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void testValidateForExportThrowsException() throws TimetableValidationException {
        doThrow(new TimetableValidationException("Invalid instructor")).when(mockValidator).validateInstructorForExport(999);

        assertThrows(TimetableValidationException.class, () ->
                provider.validateForExport(999)
        );
    }

    @Test
    @DisplayName("Should load sections for valid instructor")
    void testLoadSectionsSuccess() throws TimetableValidationException {
        Instructor mockInstructor = mock(Instructor.class);
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));
        sections.add(mock(Section.class));

        when(mockRepository.findById(456)).thenReturn(Optional.of(mockInstructor));
        when(mockInstructor.getSections()).thenReturn(sections);

        Set<Section> result = provider.loadSections(456);

        assertEquals(2, result.size());
        assertEquals(sections, result);
    }

    @Test
    @DisplayName("Should throw exception when instructor not found")
    void testLoadSectionsInstructorNotFound() {
        when(mockRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(TimetableValidationException.class, () ->
                provider.loadSections(999)
        );
    }

    @Test
    @DisplayName("Should return empty set when instructor has no sections")
    void testLoadSectionsEmptySections() throws TimetableValidationException {
        Instructor mockInstructor = mock(Instructor.class);
        when(mockRepository.findById(456)).thenReturn(Optional.of(mockInstructor));
        when(mockInstructor.getSections()).thenReturn(new HashSet<>());

        Set<Section> result = provider.loadSections(456);

        assertTrue(result.isEmpty());
    }
}