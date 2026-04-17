package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class TimetableServiceTest {

    @Mock private StudentRepository studentRepository;

    @Mock private RegistrationRecordRepository registrationRecordRepository;

    @Mock private TextTimetableFormatter defaultFormatter;

    @Mock private TextTimetableExporter defaultExporter;

    @Mock private TimetableExporter customExporter;

    private TimetableValidator validator;

    private TimetableService timetableService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new TimetableValidator(studentRepository, registrationRecordRepository);
        timetableService =
                new TimetableService(registrationRecordRepository, validator, defaultExporter);
    }

    @Test
    void exportTimetable_NullExporter_ThrowsException() {
        TimetableExportException exception =
                assertThrows(
                        TimetableExportException.class,
                        () -> timetableService.exportTimetable(12345678, null));
        assertEquals("Exporter cannot be null", exception.getMessage());
    }

    @Test
    void exportTimetable_InvalidStudentId_ThrowsValidationException() {
        TimetableValidationException exception =
                assertThrows(
                        TimetableValidationException.class,
                        () -> timetableService.exportTimetable(-1));
        assertTrue(exception.getMessage().contains("Invalid student ID"));
    }

    @Test
    void exportTimetable_StudentNotFound_ThrowsValidationException() {
        when(studentRepository.findById(99999999)).thenReturn(Optional.empty());

        TimetableValidationException exception =
                assertThrows(
                        TimetableValidationException.class,
                        () -> timetableService.exportTimetable(99999999));
        assertTrue(exception.getMessage().contains("Student not found"));
    }

    @Test
    void exportTimetable_NoRegistrationRecords_ThrowsValidationException() {
        when(studentRepository.findById(12345678)).thenReturn(Optional.of(mock(Student.class)));
        // Mocking the raw list return from the repository
        doReturn(Collections.emptyList())
                .when(registrationRecordRepository)
                .findByStudentId(12345678);

        TimetableValidationException exception =
                assertThrows(
                        TimetableValidationException.class,
                        () -> timetableService.exportTimetable(12345678));
        assertTrue(exception.getMessage().contains("no registration records"));
    }

    @Test
    void exportTimetable_SuccessWithDefaultExporter() throws Exception {
        int studentId = 12345678;
        Student mockStudent = mock(Student.class);
        List<RegistrationRecord> records = List.of(mock(RegistrationRecord.class));
        Path mockPath = Path.of("mock/path.txt");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        doReturn(records).when(registrationRecordRepository).findByStudentId(studentId);
        when(defaultExporter.export(any(TimetableData.class))).thenReturn(mockPath);

        Path result = timetableService.exportTimetable(studentId);

        assertEquals(mockPath, result);
        verify(defaultExporter, times(1)).export(any(TimetableData.class));
    }

    @Test
    void exportTimetable_SuccessWithCustomExporter() throws Exception {
        int studentId = 12345678;
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mock(Student.class)));
        doReturn(List.of(mock(RegistrationRecord.class)))
                .when(registrationRecordRepository)
                .findByStudentId(studentId);

        Path mockPath = Path.of("mock/custom.pdf");
        when(customExporter.export(any(TimetableData.class))).thenReturn(mockPath);

        Path result = timetableService.exportTimetable(studentId, customExporter);

        assertEquals(mockPath, result);
        verify(customExporter, times(1)).export(any(TimetableData.class));
        verifyNoInteractions(defaultExporter);
    }

    @Test
    void getTimetableData_Success() throws Exception {
        int studentId = 12345678;
        List<RegistrationRecord> records = List.of(mock(RegistrationRecord.class));

        doReturn(records).when(registrationRecordRepository).findByStudentId(studentId);

        TimetableData result = timetableService.getTimetableData(studentId);

        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(1, result.getRegistrationRecords().size());
    }

    @Test
    void exportTimetableWithFormatters_Success() throws Exception {
        int studentId = 12345678;
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mock(Student.class)));
        doReturn(List.of(mock(RegistrationRecord.class)))
                .when(registrationRecordRepository)
                .findByStudentId(studentId);
        when(defaultExporter.export(any(TimetableData.class))).thenReturn(Path.of("test.txt"));

        DateTimeFormatter customDay = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter customTime = DateTimeFormatter.ofPattern("HH");

        Path result =
                timetableService.exportTimetableWithFormatters(studentId, customDay, customTime);

        assertNotNull(result);
        verify(defaultExporter)
                .export(
                        argThat(
                                data ->
                                        data.getDayFormatter().equals(customDay)
                                                && data.getTimeFormatter().equals(customTime)));
    }

    @Test
    void getTimetableData_InvalidStudentId_ThrowsValidationException() {
        TimetableValidationException exception =
                assertThrows(
                        TimetableValidationException.class,
                        () -> timetableService.getTimetableData(-1));
        assertTrue(exception.getMessage().contains("Failed to build timetable data: "));
    }
}
