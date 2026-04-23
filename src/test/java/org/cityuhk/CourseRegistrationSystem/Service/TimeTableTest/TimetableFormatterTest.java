package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TimetableFormatter Interface Tests")
class TimetableFormatterTest {

    private TimetableFormatter formatter;
    private TimetableData mockTimetableData;
    private Section mockSection;

    @BeforeEach
    void setUp() {
        formatter = mock(TimetableFormatter.class);
        mockTimetableData = mock(TimetableData.class);
        mockSection = mock(Section.class);
    }

    @Test
    @DisplayName("Should format title correctly")
    void testFormatTitle() {
        String expectedTitle = "STUDENT TIMETABLE\nStudent ID: 123\n";
        when(formatter.formatTitle(mockTimetableData)).thenReturn(expectedTitle);

        String result = formatter.formatTitle(mockTimetableData);

        assertEquals(expectedTitle, result);
        verify(formatter, times(1)).formatTitle(mockTimetableData);
    }

    @Test
    @DisplayName("Should format header correctly")
    void testFormatHeader() {
        String expectedHeader = "DAY TIME COURSE\n";
        when(formatter.formatHeader()).thenReturn(expectedHeader);

        String result = formatter.formatHeader();

        assertEquals(expectedHeader, result);
        verify(formatter, times(1)).formatHeader();
    }

    @Test
    @DisplayName("Should format row correctly")
    void testFormatRow() {
        String expectedRow = "MON 09:00 CS101";
        when(formatter.formatRow(mockSection, mockTimetableData)).thenReturn(expectedRow);

        String result = formatter.formatRow(mockSection, mockTimetableData);

        assertEquals(expectedRow, result);
        verify(formatter, times(1)).formatRow(mockSection, mockTimetableData);
    }

    @Test
    @DisplayName("Should handle null timetable data in formatTitle")
    void testFormatTitleWithNullData() {
        when(formatter.formatTitle(null)).thenReturn("");

        String result = formatter.formatTitle(null);

        assertEquals("", result);
    }
}