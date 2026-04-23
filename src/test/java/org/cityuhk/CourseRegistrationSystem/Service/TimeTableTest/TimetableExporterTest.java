package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableExportException;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TimetableExporter Interface Tests")
class TimetableExporterTest {

    private TimetableExporter exporter;
    private TimetableData mockTimetableData;

    @BeforeEach
    void setUp() {
        exporter = mock(TimetableExporter.class);
        mockTimetableData = mock(TimetableData.class);
    }

    @Test
    @DisplayName("Should export timetable data successfully")
    void testExportSuccess() throws TimetableExportException {
        Path expectedPath = mock(Path.class);
        when(exporter.export(mockTimetableData)).thenReturn(expectedPath);

        Path result = exporter.export(mockTimetableData);

        assertNotNull(result);
        verify(exporter, times(1)).export(mockTimetableData);
    }

    @Test
    @DisplayName("Should throw TimetableExportException when data is invalid")
    void testExportWithInvalidData() throws TimetableExportException {
        when(exporter.export(null)).thenThrow(new TimetableExportException("Data cannot be null"));

        assertThrows(TimetableExportException.class, () -> exporter.export(null));
    }

    @Test
    @DisplayName("Should return correct file extension")
    void testGetFileExtension() {
        when(exporter.getFileExtension()).thenReturn(".txt");

        String extension = exporter.getFileExtension();

        assertEquals(".txt", extension);
    }

    @Test
    @DisplayName("Should return correct format name")
    void testGetFormatName() {
        when(exporter.getFormatName()).thenReturn("Text Format");

        String formatName = exporter.getFormatName();

        assertEquals("Text Format", formatName);
    }
}