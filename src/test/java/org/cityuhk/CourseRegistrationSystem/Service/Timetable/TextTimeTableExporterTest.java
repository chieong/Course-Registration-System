package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TextTimetableExporterTest {

    @Mock
    private TextTimetableFormatter formatter;

    @InjectMocks
    private TextTimetableExporter exporter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFileExtension_ReturnsTxt() {
        assertEquals(".txt", exporter.getFileExtension());
    }

    @Test
    void getFormatName_ReturnsText() {
        assertEquals("Text (TXT)", exporter.getFormatName());
    }

    @Test
    void export_NullData_ThrowsException() {
        TimetableExportException exception = assertThrows(TimetableExportException.class, () ->
                exporter.export(null)
        );
        assertEquals("Timetable data cannot be null", exception.getMessage());
    }

    @Test
    void export_EmptyRecords_ThrowsException() {
        TimetableData data = mock(TimetableData.class);
        when(data.getRegistrationRecords()).thenReturn(List.of());

        TimetableExportException exception = assertThrows(TimetableExportException.class, () ->
                exporter.export(data)
        );
        assertEquals("No registration records to export", exception.getMessage());
    }

    @Test
    void export_ValidData_WritesFileAndReturnsPath() throws Exception {
        // Setup mock data
        TimetableData data = mock(TimetableData.class);
        RegistrationRecord record = mock(RegistrationRecord.class);

        when(data.getStudentId()).thenReturn(12345678);
        when(data.getRegistrationRecords()).thenReturn(List.of(record));

        // Setup mock formatter behaviors
        when(formatter.formatTitle(12345678)).thenReturn("Title\n");
        when(formatter.formatHeader()).thenReturn("Header\n");
        when(formatter.formatRow(any())).thenReturn("RowData");

        // Execute
        Path outputPath = exporter.export(data);

        // Verify
        assertNotNull(outputPath);
        assertTrue(Files.exists(outputPath));

        String fileContent = Files.readString(outputPath);
        assertTrue(fileContent.contains("Title"));
        assertTrue(fileContent.contains("Header"));
        assertTrue(fileContent.contains("RowData"));

        // Clean up system temp file
        Files.deleteIfExists(outputPath);
    }
}