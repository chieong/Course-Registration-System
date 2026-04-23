package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Model.Section;

import org.cityuhk.CourseRegistrationSystem.Service.Timetable.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TimetableService Tests")
class TimetableServiceTest {

    private TimetableService service;
    private TimetableValidator mockValidator;
    private TextTimetableExporter mockDefaultExporter;
    private StudentTimetableOwnerProvider mockStudentProvider;
    private InstructorTimetableOwnerProvider mockInstructorProvider;

    @BeforeEach
    void setUp() {
        mockValidator = mock(TimetableValidator.class);
        mockDefaultExporter = mock(TextTimetableExporter.class);
        mockStudentProvider = mock(StudentTimetableOwnerProvider.class);
        mockInstructorProvider = mock(InstructorTimetableOwnerProvider.class);

        service = new TimetableService(mockValidator, mockDefaultExporter, mockStudentProvider, mockInstructorProvider);
    }

    @Test
    @DisplayName("Should export student timetable successfully")
    void testExportStudentTimetableSuccess() throws TimetableExportException, TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));
        Path mockPath = mock(Path.class);

        when(mockStudentProvider.userType()).thenReturn(TimetableData.UserType.Student);
        when(mockStudentProvider.ownerIdLabel()).thenReturn("Student ID");
        doNothing().when(mockStudentProvider).validateForExport(123);
        when(mockStudentProvider.loadSections(123)).thenReturn(sections);
        when(mockDefaultExporter.export(any())).thenReturn(mockPath);

        Path result = service.exportStudentTimetable(123);

        assertNotNull(result);
        verify(mockValidator, times(1)).validateTimetableData(any());
    }

    @Test
    @DisplayName("Should export student timetable with custom exporter")
    void testExportStudentTimetableWithCustomExporter() throws TimetableExportException, TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));
        Path mockPath = mock(Path.class);
        TimetableExporter customExporter = mock(TimetableExporter.class);

        when(mockStudentProvider.userType()).thenReturn(TimetableData.UserType.Student);
        when(mockStudentProvider.ownerIdLabel()).thenReturn("Student ID");
        doNothing().when(mockStudentProvider).validateForExport(123);
        when(mockStudentProvider.loadSections(123)).thenReturn(sections);
        when(customExporter.export(any())).thenReturn(mockPath);

        Path result = service.exportStudentTimetable(123, customExporter);

        assertNotNull(result);
        verify(mockValidator, times(1)).validateTimetableData(any());
    }

    @Test
    @DisplayName("Should export student timetable with custom formatters")
    void testExportStudentTimetableWithFormatters() throws TimetableExportException, TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));
        Path mockPath = mock(Path.class);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        when(mockStudentProvider.userType()).thenReturn(TimetableData.UserType.Student);
        when(mockStudentProvider.ownerIdLabel()).thenReturn("Student ID");
        doNothing().when(mockStudentProvider).validateForExport(123);
        when(mockStudentProvider.loadSections(123)).thenReturn(sections);
        when(mockDefaultExporter.export(any())).thenReturn(mockPath);

        Path result = service.exportStudentTimetableWithFormatters(123, dayFormatter, timeFormatter);

        assertNotNull(result);
        verify(mockValidator, times(1)).validateTimetableData(any());
    }

    @Test
    @DisplayName("Should get student timetable data")
    void testGetStudentTimetableData() throws TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));

        when(mockStudentProvider.userType()).thenReturn(TimetableData.UserType.Student);
        when(mockStudentProvider.ownerIdLabel()).thenReturn("Student ID");
        doNothing().when(mockStudentProvider).validateForExport(123);
        when(mockStudentProvider.loadSections(123)).thenReturn(sections);

        TimetableData result = service.getStudentTimetableData(123);

        assertNotNull(result);
        assertEquals(123, result.getOwnerId());
        assertEquals(TimetableData.UserType.Student, result.getUserType());
    }

    @Test
    @DisplayName("Should export instructor timetable successfully")
    void testExportInstructorTimetableSuccess() throws TimetableExportException, TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));
        Path mockPath = mock(Path.class);

        when(mockInstructorProvider.userType()).thenReturn(TimetableData.UserType.Instructor);
        when(mockInstructorProvider.ownerIdLabel()).thenReturn("Staff ID");
        doNothing().when(mockInstructorProvider).validateForExport(456);
        when(mockInstructorProvider.loadSections(456)).thenReturn(sections);
        when(mockDefaultExporter.export(any())).thenReturn(mockPath);

        Path result = service.exportInstructorTimetable(456);

        assertNotNull(result);
        verify(mockValidator, times(1)).validateTimetableData(any());
    }

    @Test
    @DisplayName("Should export instructor timetable with custom exporter")
    void testExportInstructorTimetableWithCustomExporter() throws TimetableExportException, TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));
        Path mockPath = mock(Path.class);
        TimetableExporter customExporter = mock(TimetableExporter.class);

        when(mockInstructorProvider.userType()).thenReturn(TimetableData.UserType.Instructor);
        when(mockInstructorProvider.ownerIdLabel()).thenReturn("Staff ID");
        doNothing().when(mockInstructorProvider).validateForExport(456);
        when(mockInstructorProvider.loadSections(456)).thenReturn(sections);
        when(customExporter.export(any())).thenReturn(mockPath);

        Path result = service.exportInstructorTimetable(456, customExporter);

        assertNotNull(result);
        verify(mockValidator, times(1)).validateTimetableData(any());
    }

    @Test
    @DisplayName("Should get instructor timetable data")
    void testGetInstructorTimetableData() throws TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));

        when(mockInstructorProvider.userType()).thenReturn(TimetableData.UserType.Instructor);
        when(mockInstructorProvider.ownerIdLabel()).thenReturn("Staff ID");
        doNothing().when(mockInstructorProvider).validateForExport(456);
        when(mockInstructorProvider.loadSections(456)).thenReturn(sections);

        TimetableData result = service.getInstructorTimetableData(456);

        assertNotNull(result);
        assertEquals(456, result.getOwnerId());
        assertEquals(TimetableData.UserType.Instructor, result.getUserType());
    }

    @Test
    @DisplayName("Should throw exception when owner provider is null in exportTimetable")
    void testExportTimetableWithNullOwnerProvider() {
        assertThrows(TimetableValidationException.class, () ->
                service.exportTimetable(123, null, mockDefaultExporter)
        );
    }

    @Test
    @DisplayName("Should throw exception when exporter is null in exportTimetable")
    void testExportTimetableWithNullExporter() {
        assertThrows(TimetableExportException.class, () ->
                service.exportTimetable(123, mockStudentProvider, null)
        );
    }

    @Test
    @DisplayName("Should export timetable using generic method")
    void testExportTimetableGeneric() throws TimetableExportException, TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));
        Path mockPath = mock(Path.class);

        when(mockStudentProvider.userType()).thenReturn(TimetableData.UserType.Student);
        when(mockStudentProvider.ownerIdLabel()).thenReturn("Student ID");
        doNothing().when(mockStudentProvider).validateForExport(123);
        when(mockStudentProvider.loadSections(123)).thenReturn(sections);
        when(mockDefaultExporter.export(any())).thenReturn(mockPath);

        Path result = service.exportTimetable(123, mockStudentProvider, mockDefaultExporter);

        assertNotNull(result);
        verify(mockValidator, times(1)).validateTimetableData(any());
    }

    @Test
    @DisplayName("Should throw validation exception when validation fails during export")
    void testExportStudentTimetableValidationFails() throws TimetableValidationException {
        doThrow(new TimetableValidationException("Validation failed"))
                .when(mockStudentProvider).validateForExport(999);

        assertThrows(TimetableValidationException.class, () ->
                service.exportStudentTimetable(999)
        );
    }
}