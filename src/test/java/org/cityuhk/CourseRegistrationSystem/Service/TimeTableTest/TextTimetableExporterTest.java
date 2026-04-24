package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TextTimetableExporter;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TextTimetableFormatter;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableExportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TextTimetableExporter Tests")
class TextTimetableExporterTest {

    private TextTimetableExporter exporter;
    private TextTimetableFormatter mockFormatter;
    private TimetableData mockTimetableData;
    private Section mockSection;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockFormatter = mock(TextTimetableFormatter.class);
        exporter = new TextTimetableExporter(mockFormatter);
        mockTimetableData = mock(TimetableData.class);
        mockSection = mock(Section.class);
    }

    @Test
    @DisplayName("Should export timetable successfully")
    void testExportSuccess() throws TimetableExportException {
        Set<Section> sections = new HashSet<>();
        sections.add(mockSection);

        when(mockTimetableData.getSections()).thenReturn(sections);
        when(mockTimetableData.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(mockTimetableData.getOwnerId()).thenReturn(123);
        when(mockFormatter.formatTitle(mockTimetableData)).thenReturn("Title\n");
        when(mockFormatter.formatHeader()).thenReturn("Header\n");
        when(mockFormatter.formatRow(mockSection, mockTimetableData)).thenReturn("Row");
        when(mockSection.compareTo(any())).thenReturn(0);

        Path result = exporter.export(mockTimetableData);

        assertNotNull(result);
        assertTrue(Files.exists(result));
    }

    @Test
    @DisplayName("Should throw exception when timetable data is null")
    void testExportWithNullData() {
        assertThrows(TimetableExportException.class, () -> exporter.export(null));
    }

    @Test
    @DisplayName("Should throw exception when sections are null")
    void testExportWithNullSections() {
        when(mockTimetableData.getSections()).thenReturn(null);

        assertThrows(TimetableExportException.class, () -> exporter.export(mockTimetableData));
    }

    @Test
    @DisplayName("Should throw exception when sections are empty")
    void testExportWithEmptySections() {
        when(mockTimetableData.getSections()).thenReturn(new HashSet<>());

        assertThrows(TimetableExportException.class, () -> exporter.export(mockTimetableData));
    }

    @Test
    @DisplayName("Should return correct file extension")
    void testGetFileExtension() {
        assertEquals(".txt", exporter.getFileExtension());
    }

    @Test
    @DisplayName("Should return correct format name")
    void testGetFormatName() {
        assertEquals("Text (TXT)", exporter.getFormatName());
    }

    @Test
    @DisplayName("Should handle null formatted row")
    void testExportWithNullFormattedRow() throws TimetableExportException {
        Set<Section> sections = new HashSet<>();
        sections.add(mockSection);

        when(mockTimetableData.getSections()).thenReturn(sections);
        when(mockTimetableData.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(mockTimetableData.getOwnerId()).thenReturn(123);
        when(mockFormatter.formatTitle(mockTimetableData)).thenReturn("Title\n");
        when(mockFormatter.formatHeader()).thenReturn("Header\n");
        when(mockFormatter.formatRow(mockSection, mockTimetableData)).thenReturn(null);
        when(mockSection.compareTo(any())).thenReturn(0);

        Path result = exporter.export(mockTimetableData);

        assertNotNull(result);
        assertTrue(Files.exists(result));
    }

    @Test
    @DisplayName("Should create temp file with correct naming pattern")
    void testTempFileNaming() throws TimetableExportException {
        Set<Section> sections = new HashSet<>();
        sections.add(mockSection);

        when(mockTimetableData.getSections()).thenReturn(sections);
        when(mockTimetableData.getUserType()).thenReturn(TimetableData.UserType.Instructor);
        when(mockTimetableData.getOwnerId()).thenReturn(456);
        when(mockFormatter.formatTitle(mockTimetableData)).thenReturn("Title\n");
        when(mockFormatter.formatHeader()).thenReturn("Header\n");
        when(mockFormatter.formatRow(mockSection, mockTimetableData)).thenReturn("Row");
        when(mockSection.compareTo(any())).thenReturn(0);

        Path result = exporter.export(mockTimetableData);

        String filename = result.getFileName().toString();
        assertTrue(filename.contains("Instructor456-timetable-"));
        assertTrue(filename.endsWith(".txt"));
    }

    @Test
    @DisplayName("Should write file with UTF-8 encoding")
    void testFileEncoding() throws TimetableExportException, IOException {
        Set<Section> sections = new HashSet<>();
        sections.add(mockSection);

        when(mockTimetableData.getSections()).thenReturn(sections);
        when(mockTimetableData.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(mockTimetableData.getOwnerId()).thenReturn(789);
        when(mockFormatter.formatTitle(mockTimetableData)).thenReturn("Title\n");
        when(mockFormatter.formatHeader()).thenReturn("Header\n");
        when(mockFormatter.formatRow(mockSection, mockTimetableData)).thenReturn("Row");
        when(mockSection.compareTo(any())).thenReturn(0);

        Path result = exporter.export(mockTimetableData);

        String content = new String(Files.readAllBytes(result));
        assertTrue(content.contains("Title"));
        assertTrue(content.contains("Header"));
    }

    @Test
    @DisplayName("Should throw exception when writeToFile fails")
    void testExportCatchBlock() {
        Set<Section> sections = new HashSet<>();
        sections.add(mockSection);

        when(mockTimetableData.getSections()).thenReturn(sections);
        when(mockTimetableData.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(mockTimetableData.getOwnerId()).thenReturn(123);

        // 🔥 Force exception inside writeToFile()
        when(mockFormatter.formatTitle(any()))
                .thenThrow(new RuntimeException("IO failure"));

        when(mockSection.compareTo(any())).thenReturn(0);

        TimetableExportException ex = assertThrows(
                TimetableExportException.class,
                () -> exporter.export(mockTimetableData));

        assertTrue(ex.getMessage().contains("Failed to export timetable"));
    }

    @Test
    @DisplayName("Should throw exception when print data is null")
    void testPrintWithNullData() {
        assertThrows(TimetableExportException.class,
                () -> exporter.print(null));
    }

    @Test
    @DisplayName("Should throw exception when printing empty sections")
    void testPrintWithEmptySections() {
        when(mockTimetableData.getSections()).thenReturn(new HashSet<>());

        assertThrows(TimetableExportException.class,
                () -> exporter.print(mockTimetableData));
    }

    @Test
    @DisplayName("Should print timetable successfully")
    void testPrintSuccess() throws TimetableExportException {
        Set<Section> sections = new HashSet<>();
        sections.add(mockSection);

        when(mockTimetableData.getSections()).thenReturn(sections);
        when(mockFormatter.formatTitle(mockTimetableData)).thenReturn("Title\n");
        when(mockFormatter.formatHeader()).thenReturn("Header\n");
        when(mockFormatter.formatRow(mockSection, mockTimetableData)).thenReturn("Row");
        when(mockSection.compareTo(any())).thenReturn(0);

        String result = exporter.print(mockTimetableData);

        assertNotNull(result);
        assertTrue(result.contains("Title"));
        assertTrue(result.contains("Header"));
        assertTrue(result.contains("Row"));
    }

    @Test
    @DisplayName("Should throw exception when printToString fails")
    void testPrintCatchBlock() {
        Set<Section> sections = new HashSet<>();
        sections.add(mockSection);

        when(mockTimetableData.getSections()).thenReturn(sections);

        // 🔥 Force exception
        when(mockFormatter.formatTitle(any()))
                .thenThrow(new RuntimeException("formatter crash"));

        when(mockSection.compareTo(any())).thenReturn(0);

        TimetableExportException ex = assertThrows(
                TimetableExportException.class,
                () -> exporter.print(mockTimetableData));

        assertTrue(ex.getMessage().contains("Failed to fetch timetable data"));
    }

}