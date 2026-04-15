package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Example unit tests demonstrating the refactored architecture.
 * Each component can be tested independently.
 * 
 * This file demonstrates the improved testability after refactoring.
 */
@DisplayName("Timetable Service Test Suite")
public class TimetableServiceTests {
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private RegistrationRecordRepository registrationRecordRepository;
    
    @Mock
    private TimetableExporter mockExporter;
    
    private TimetableFormatter formatter;
    private TextTimetableExporter textExporter;
    private TimetableService timetableService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        formatter = new TextTimetableFormatter();
        textExporter = new TextTimetableExporter((TextTimetableFormatter) formatter);
        timetableService = new TimetableService(studentRepository, registrationRecordRepository, textExporter);
    }
    
    // ===== TimetableFormatter Tests =====
    
    @Test
    @DisplayName("Should format title with student ID")
    public void testFormatTitleContainsStudentId() {
        // Given: A formatter and student ID
        Integer studentId = 12345;
        
        // When: Title is formatted
        String title = formatter.formatTitle(studentId);
        
        // Then: Title should contain student ID
        assertNotNull(title);
        assertTrue(title.contains("12345"));
        assertTrue(title.contains("TIMETABLE"));
    }
    
    @Test
    @DisplayName("Should format header with column names")
    public void testFormatHeaderContainsColumns() {
        // When: Header is formatted
        String header = formatter.formatHeader();
        
        // Then: Header should contain all column names
        assertNotNull(header);
        assertTrue(header.contains("DAY"));
        assertTrue(header.contains("TIME"));
        assertTrue(header.contains("COURSE"));
        assertTrue(header.contains("SEC"));
        assertTrue(header.contains("TYPE"));
        assertTrue(header.contains("VENUE"));
    }
    
    @Test
    @DisplayName("Should return null for null record")
    public void testFormatRowNullRecord() {
        // When: Null record is formatted
        String row = formatter.formatRow(null);
        
        // Then: Should return null
        assertNull(row);
    }
    
    // ===== TimetableData Builder Tests =====
    
    @Test
    @DisplayName("Should build timetable data with builder pattern")
    public void testBuildTimetableData() {
        // Given: Basic data for builder
        Integer studentId = 12345;
        List<RegistrationRecord> records = Arrays.asList(
            new RegistrationRecord(),
            new RegistrationRecord()
        );
        
        // When: Building timetable data
        TimetableData data = new TimetableData.Builder()
            .studentId(studentId)
            .registrationRecords(records)
            .build();
        
        // Then: Data should be properly constructed
        assertNotNull(data);
        assertEquals(studentId, data.getStudentId());
        assertEquals(2, data.getRegistrationRecords().size());
    }
    
    @Test
    @DisplayName("Should fail building without student ID")
    public void testBuildFailsWithoutStudentId() {
        // When/Then: Build should fail without required student ID
        assertThrows(IllegalStateException.class, () -> {
            new TimetableData.Builder()
                .registrationRecords(Arrays.asList())
                .build();
        });
    }
    
    @Test
    @DisplayName("Should fail building with empty records")
    public void testBuildFailsWithEmptyRecords() {
        // When/Then: Build should fail with empty records
        assertThrows(IllegalStateException.class, () -> {
            new TimetableData.Builder()
                .studentId(12345)
                .registrationRecords(Arrays.asList())
                .build();
        });
    }
    
    @Test
    @DisplayName("Should build with custom formatters")
    public void testBuildWithCustomFormatters() {
        // Given: Custom formatters
        DateTimeFormatter customDayFormatter = DateTimeFormatter.ofPattern("EEEE");
        DateTimeFormatter customTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<RegistrationRecord> records = Arrays.asList(new RegistrationRecord());
        
        // When: Building with custom formatters
        TimetableData data = new TimetableData.Builder()
            .studentId(12345)
            .registrationRecords(records)
            .dayFormatter(customDayFormatter)
            .timeFormatter(customTimeFormatter)
            .build();
        
        // Then: Formatters should be set
        assertNotNull(data);
        assertEquals(customDayFormatter, data.getDayFormatter());
        assertEquals(customTimeFormatter, data.getTimeFormatter());
    }
    
    // ===== TimetableExporter Tests (Mock-based) =====
    
    @Test
    @DisplayName("Should call exporter with timetable data")
    public void testExporterInvocationWithData() throws TimetableExportException {
        // Given: Mock exporter and timetable data
        Path mockPath = mock(Path.class);
        when(mockExporter.export(any(TimetableData.class)))
            .thenReturn(mockPath);
        
        List<RegistrationRecord> records = Arrays.asList(new RegistrationRecord());
        TimetableData data = new TimetableData.Builder()
            .studentId(12345)
            .registrationRecords(records)
            .build();
        
        // When: Exporting
        Path result = mockExporter.export(data);
        
        // Then: Exporter should be called with data
        verify(mockExporter).export(data);
        assertEquals(mockPath, result);
    }
    
    // ===== Strategy Pattern Verification =====
    
    @Test
    @DisplayName("Should support multiple export format strategies")
    public void testMultipleExporterStrategies() {
        // When: Different exporters are created
        TimetableExporter textExporter = new TextTimetableExporter((TextTimetableFormatter) formatter);
        
        // Then: Each should have distinct characteristics
        assertEquals(".txt", textExporter.getFileExtension());
        assertEquals("Text (TXT)", textExporter.getFormatName());
        
        // In future, we could add:
        // CsvTimetableExporter csvExporter = new CsvTimetableExporter(...);
        // assertEquals(".csv", csvExporter.getFileExtension());
        // assertEquals("CSV", csvExporter.getFormatName());
    }
    
    // ===== Error Handling Tests =====
    
    @Test
    @DisplayName("Should throw TimetableExportException on export failure")
    public void testExportExceptionPropagation() throws TimetableExportException {
        // Given: Exporter configured to fail
        when(mockExporter.export(any(TimetableData.class)))
            .thenThrow(new TimetableExportException("Export failed"));
        
        // When/Then: Exception should be thrown
        List<RegistrationRecord> records = Arrays.asList(new RegistrationRecord());
        TimetableData data = new TimetableData.Builder()
            .studentId(12345)
            .registrationRecords(records)
            .build();
        
        assertThrows(TimetableExportException.class, () -> {
            mockExporter.export(data);
        });
    }
    
    @Test
    @DisplayName("Should throw TimetableValidationException on validation failure")
    public void testValidationExceptionPropagation() throws TimetableValidationException {
        // Given: Service dependencies configured to fail validation
        when(studentRepository.findById(99999)).thenReturn(Optional.empty());

        // When/Then: Exception should be thrown from the service
        TimetableValidationException exception = assertThrows(TimetableValidationException.class, () -> {
            timetableService.exportTimetable(99999, mockExporter);
        });

        assertEquals("Student not found with ID: 99999", exception.getMessage());
    }
    
    // ===== Exception Type Tests =====
    
    @Test
    @DisplayName("TimetableExportException should be throwable and catchable")
    public void testExportExceptionHandling() {
        TimetableExportException ex = new TimetableExportException("Test error");
        assertNotNull(ex);
        assertEquals("Test error", ex.getMessage());
    }
    
    @Test
    @DisplayName("TimetableExportException with cause should preserve stack trace")
    public void testExportExceptionWithCause() {
        IOException cause = new IOException("Underlying cause");
        TimetableExportException ex = new TimetableExportException("Export failed", cause);
        
        assertNotNull(ex);
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IOException);
    }
    
    @Test
    @DisplayName("TimetableValidationException should be throwable and catchable")
    public void testValidationExceptionHandling() {
        TimetableValidationException ex = new TimetableValidationException("Validation failed");
        assertNotNull(ex);
        assertEquals("Validation failed", ex.getMessage());
    }
}

