package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.StudentTimetableOwnerProvider;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidationException;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StudentTimetableOwnerProvider Tests")
class StudentTimetableOwnerProviderTest {

    private StudentTimetableOwnerProvider provider;
    private TimetableValidator mockValidator;
    private RegistrationRecordRepositoryPort mockRepository;

    @BeforeEach
    void setUp() {
        mockValidator = mock(TimetableValidator.class);
        mockRepository = mock(RegistrationRecordRepositoryPort.class);
        provider = new StudentTimetableOwnerProvider(mockValidator, mockRepository);
    }

    @Test
    @DisplayName("Should return Student user type")
    void testUserType() {
        assertEquals(TimetableData.UserType.Student, provider.userType());
    }

    @Test
    @DisplayName("Should return correct owner ID label")
    void testOwnerIdLabel() {
        assertEquals("Student ID", provider.ownerIdLabel());
    }

    @Test
    @DisplayName("Should validate student for export successfully")
    void testValidateForExportSuccess() throws TimetableValidationException {
        assertDoesNotThrow(() -> provider.validateForExport(123));
        verify(mockValidator, times(1)).validateStudentForExport(123);
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void testValidateForExportThrowsException() throws TimetableValidationException {
        doThrow(new TimetableValidationException("Invalid student")).when(mockValidator).validateStudentForExport(999);

        assertThrows(TimetableValidationException.class, () ->
                provider.validateForExport(999)
        );
    }

    @Test
    @DisplayName("Should load sections for valid student")
    void testLoadSectionsSuccess() throws TimetableValidationException {
        RegistrationRecord record1 = mock(RegistrationRecord.class);
        RegistrationRecord record2 = mock(RegistrationRecord.class);
        Section section1 = mock(Section.class);
        Section section2 = mock(Section.class);

        when(record1.getSection()).thenReturn(section1);
        when(record2.getSection()).thenReturn(section2);

        List<RegistrationRecord> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        when(mockRepository.findByStudentId(123)).thenReturn(records);

        Set<Section> result = provider.loadSections(123);

        assertEquals(2, result.size());
        assertTrue(result.contains(section1));
        assertTrue(result.contains(section2));
    }

    @Test
    @DisplayName("Should handle null registration records")
    void testLoadSectionsWithNullRecords() throws TimetableValidationException {
        when(mockRepository.findByStudentId(123)).thenReturn(null);

        Set<Section> result = provider.loadSections(123);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should skip null records in the list")
    void testLoadSectionsWithNullRecordInList() throws TimetableValidationException {
        RegistrationRecord record1 = mock(RegistrationRecord.class);
        Section section1 = mock(Section.class);

        when(record1.getSection()).thenReturn(section1);

        List<RegistrationRecord> records = new ArrayList<>();
        records.add(record1);
        records.add(null);

        when(mockRepository.findByStudentId(123)).thenReturn(records);

        Set<Section> result = provider.loadSections(123);

        assertEquals(1, result.size());
        assertTrue(result.contains(section1));
    }

    @Test
    @DisplayName("Should skip records with null section")
    void testLoadSectionsWithNullSection() throws TimetableValidationException {
        RegistrationRecord record1 = mock(RegistrationRecord.class);
        RegistrationRecord record2 = mock(RegistrationRecord.class);
        Section section1 = mock(Section.class);

        when(record1.getSection()).thenReturn(section1);
        when(record2.getSection()).thenReturn(null);

        List<RegistrationRecord> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        when(mockRepository.findByStudentId(123)).thenReturn(records);

        Set<Section> result = provider.loadSections(123);

        assertEquals(1, result.size());
        assertTrue(result.contains(section1));
    }

    @Test
    @DisplayName("Should return empty set when no valid records")
    void testLoadSectionsWithNoValidRecords() throws TimetableValidationException {
        RegistrationRecord record = mock(RegistrationRecord.class);
        when(record.getSection()).thenReturn(null);

        List<RegistrationRecord> records = new ArrayList<>();
        records.add(record);

        when(mockRepository.findByStudentId(123)).thenReturn(records);

        Set<Section> result = provider.loadSections(123);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should avoid duplicate sections")
    void testLoadSectionsWithDuplicates() throws TimetableValidationException {
        RegistrationRecord record1 = mock(RegistrationRecord.class);
        RegistrationRecord record2 = mock(RegistrationRecord.class);
        Section section = mock(Section.class);

        when(record1.getSection()).thenReturn(section);
        when(record2.getSection()).thenReturn(section);

        List<RegistrationRecord> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        when(mockRepository.findByStudentId(123)).thenReturn(records);

        Set<Section> result = provider.loadSections(123);

        assertEquals(1, result.size());
    }
}