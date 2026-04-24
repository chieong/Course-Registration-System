package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TextTimetableFormatter;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TextTimetableFormatter Tests")
class TextTimetableFormatterTest {

    private TextTimetableFormatter formatter;
    private TimetableData mockTimetableData;
    private Section mockSection;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        formatter = new TextTimetableFormatter();
        mockTimetableData = mock(TimetableData.class);
        mockSection = mock(Section.class);
        mockCourse = mock(Course.class);
    }

    @Test
    @DisplayName("Should format title with all required information")
    void testFormatTitleComplete() {
        when(mockTimetableData.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(mockTimetableData.getOwnerIdLabel()).thenReturn("Student ID");
        when(mockTimetableData.getOwnerId()).thenReturn(12345);

        String result = formatter.formatTitle(mockTimetableData);
        System.out.println(result);
        assertTrue(result.contains("Student TIMETABLE"));
        assertTrue(result.contains("Student ID: 12345"));
        assertTrue(result.contains("Generated At:"));
    }

    @Test
    @DisplayName("Should format title with instructor type")
    void testFormatTitleForInstructor() {
        when(mockTimetableData.getUserType()).thenReturn(TimetableData.UserType.Instructor);
        when(mockTimetableData.getOwnerIdLabel()).thenReturn("Staff ID");
        when(mockTimetableData.getOwnerId()).thenReturn(54321);

        String result = formatter.formatTitle(mockTimetableData);
        System.out.println(result);
        assertTrue(result.contains("Instructor TIMETABLE"));
        assertTrue(result.contains("Staff ID: 54321"));
    }

    @Test
    @DisplayName("Should return empty string when timetable data is null")
    void testFormatTitleWithNull() {
        String result = formatter.formatTitle(null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("Should format header with correct structure")
    void testFormatHeader() {
        String result = formatter.formatHeader();

        assertTrue(result.contains("DAY"));
        assertTrue(result.contains("TIME"));
        assertTrue(result.contains("COURSE"));
        assertTrue(result.contains("SEC"));
        assertTrue(result.contains("TYPE"));
        assertTrue(result.contains("VENUE"));
        assertTrue(result.contains("------"));
    }

    @Test
    @DisplayName("Should format row with complete section information")
    void testFormatRowComplete() {
        LocalDateTime startTime = LocalDateTime.of(2024, 4, 23, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 4, 23, 10, 30);

        when(mockSection.getCourse()).thenReturn(mockCourse);
        when(mockCourse.getCourseCode()).thenReturn("CS101");
        when(mockSection.getType()).thenReturn(mock(Section.Type.class));
        when(mockSection.getType().name()).thenReturn("Lecture");
        when(mockSection.getVenue()).thenReturn("Room 101");
        when(mockSection.getStartTime()).thenReturn(startTime);
        when(mockSection.getEndTime()).thenReturn(endTime);
        when(mockSection.getSectionId()).thenReturn(1);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        when(mockTimetableData.getDayFormatter()).thenReturn(dayFormatter);
        when(mockTimetableData.getTimeFormatter()).thenReturn(timeFormatter);

        String result = formatter.formatRow(mockSection, mockTimetableData);

        assertTrue(result.contains("Tue"));
        assertTrue(result.contains("09:00-10:30"));
        assertTrue(result.contains("CS101"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Lecture"));
        assertTrue(result.contains("Room 101"));
    }

    @Test
    @DisplayName("Should format row with null section")
    void testFormatRowWithNullSection() {
        String result = formatter.formatRow(null, mockTimetableData);

        assertEquals("", result);
    }

    @Test
    @DisplayName("Should format row with null course")
    void testFormatRowWithNullCourse() {
        LocalDateTime startTime = LocalDateTime.of(2024, 4, 23, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 4, 23, 10, 30);

        when(mockSection.getCourse()).thenReturn(null);
        when(mockSection.getType()).thenReturn(mock(Section.Type.class));
        when(mockSection.getType().name()).thenReturn("Lecture");
        when(mockSection.getVenue()).thenReturn("Room 101");
        when(mockSection.getStartTime()).thenReturn(startTime);
        when(mockSection.getEndTime()).thenReturn(endTime);
        when(mockSection.getSectionId()).thenReturn(1);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        when(mockTimetableData.getDayFormatter()).thenReturn(dayFormatter);
        when(mockTimetableData.getTimeFormatter()).thenReturn(timeFormatter);

        String result = formatter.formatRow(mockSection, mockTimetableData);

        assertNotNull(result);
        assertTrue(result.contains("1"));
    }

    @Test
    @DisplayName("Should handle null start and end times")
    void testFormatRowWithNullTimes() {
        when(mockSection.getCourse()).thenReturn(mockCourse);
        when(mockCourse.getCourseCode()).thenReturn("CS101");
        when(mockSection.getType()).thenReturn(mock(Section.Type.class));
        when(mockSection.getType().name()).thenReturn("Tutorial");
        when(mockSection.getVenue()).thenReturn("Lab");
        when(mockSection.getStartTime()).thenReturn(null);
        when(mockSection.getEndTime()).thenReturn(null);
        when(mockSection.getSectionId()).thenReturn(2);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        when(mockTimetableData.getDayFormatter()).thenReturn(dayFormatter);
        when(mockTimetableData.getTimeFormatter()).thenReturn(timeFormatter);

        String result = formatter.formatRow(mockSection, mockTimetableData);

        assertTrue(result.contains("N/A"));
    }

    @Test
    @DisplayName("Should trim long course code")
    void testTrimLongCourseCode() {
        LocalDateTime startTime = LocalDateTime.of(2024, 4, 23, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 4, 23, 10, 30);

        when(mockSection.getCourse()).thenReturn(mockCourse);
        when(mockCourse.getCourseCode()).thenReturn("VERYLONGCOURSECODETHATEXCEEDSWIDTH");
        when(mockSection.getType()).thenReturn(mock(Section.Type.class));
        when(mockSection.getType().name()).thenReturn("Lecture");
        when(mockSection.getVenue()).thenReturn("Room 101");
        when(mockSection.getStartTime()).thenReturn(startTime);
        when(mockSection.getEndTime()).thenReturn(endTime);
        when(mockSection.getSectionId()).thenReturn(1);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        when(mockTimetableData.getDayFormatter()).thenReturn(dayFormatter);
        when(mockTimetableData.getTimeFormatter()).thenReturn(timeFormatter);

        String result = formatter.formatRow(mockSection, mockTimetableData);

        assertTrue(result.contains("..."));
    }

    @Test
    @DisplayName("Should use default formatters when timetable data formatters are null")
    void testFormatRowWithNullFormatters() {
        LocalDateTime startTime = LocalDateTime.of(2024, 4, 23, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 4, 23, 10, 30);

        when(mockSection.getCourse()).thenReturn(mockCourse);
        when(mockCourse.getCourseCode()).thenReturn("CS101");
        when(mockSection.getType()).thenReturn(mock(Section.Type.class));
        when(mockSection.getType().name()).thenReturn("Lecture");
        when(mockSection.getVenue()).thenReturn("Room 101");
        when(mockSection.getStartTime()).thenReturn(startTime);
        when(mockSection.getEndTime()).thenReturn(endTime);
        when(mockSection.getSectionId()).thenReturn(1);

        when(mockTimetableData.getDayFormatter()).thenReturn(null);
        when(mockTimetableData.getTimeFormatter()).thenReturn(null);

        String result = formatter.formatRow(mockSection, mockTimetableData);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle null type field")
    void testFormatRowWithNullType() {
        LocalDateTime startTime = LocalDateTime.of(2024, 4, 23, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 4, 23, 10, 30);

        when(mockSection.getCourse()).thenReturn(mockCourse);
        when(mockCourse.getCourseCode()).thenReturn("CS101");
        when(mockSection.getType()).thenReturn(null);
        when(mockSection.getVenue()).thenReturn("Room 101");
        when(mockSection.getStartTime()).thenReturn(startTime);
        when(mockSection.getEndTime()).thenReturn(endTime);
        when(mockSection.getSectionId()).thenReturn(1);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        when(mockTimetableData.getDayFormatter()).thenReturn(dayFormatter);
        when(mockTimetableData.getTimeFormatter()).thenReturn(timeFormatter);

        String result = formatter.formatRow(mockSection, mockTimetableData);

        assertNotNull(result);
    }

    @Test 
    void coverTrimToWidthNullViaReflection() throws Exception {
        Method m = TextTimetableFormatter.class
                .getDeclaredMethod("trimToWidth", String.class, int.class);
        m.setAccessible(true);

        String result = (String) m.invoke(formatter, null, 12);
        assertEquals("", result);
    }


}