package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TextTimetableFormatterTest {

    private TextTimetableFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new TextTimetableFormatter();
    }

    @Test
    void formatTitle_ContainsStudentId() {
        String title = formatter.formatTitle(55555555);
        assertTrue(title.contains("STUDENT TIMETABLE"));
        assertTrue(title.contains("55555555"));
        assertTrue(title.contains("Generated At:"));
    }

    @Test
    void formatHeader_ContainsColumns() {
        String header = formatter.formatHeader();
        assertTrue(header.contains("DAY"));
        assertTrue(header.contains("COURSE"));
        assertTrue(header.contains("VENUE"));
    }

    @Test
    void formatRow_ValidRecord_FormatsCorrectly() {
        // Using RETURNS_DEEP_STUBS to bypass creating actual model instances
        RegistrationRecord record = mock(RegistrationRecord.class, Answers.RETURNS_DEEP_STUBS);

        when(record.getSection().getCourse().getCourseCode()).thenReturn("CS3343");
        when(record.getSection().getSectionID()).thenReturn(1);
        when(record.getSection().getType().name()).thenReturn("LECTURE");
        when(record.getSection().getVenue()).thenReturn("AC2-Canteen-Wing");

        LocalDateTime start = LocalDateTime.of(2026, 4, 16, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 16, 11, 50);
        when(record.getSection().getStartTime()).thenReturn(start);
        when(record.getSection().getEndTime()).thenReturn(end);

        String row = formatter.formatRow(record);

        assertNotNull(row);
        assertTrue(row.contains("CS3343"));
        assertTrue(row.contains("Thu")); // April 16 2026 is a Thursday
        assertTrue(row.contains("10:00-11:50"));
        assertTrue(row.contains("AC2-Canteen-Wing"));
    }

    @Test
    void formatRow_NullRecordOrSection_ReturnsNull() {
        assertNull(formatter.formatRow(null));

        RegistrationRecord recordNoSection = mock(RegistrationRecord.class);
        when(recordNoSection.getSection()).thenReturn(null);
        assertNull(formatter.formatRow(recordNoSection));
    }

    @Test
    void formatRow_LongStrings_TrimsCorrectly() {
        RegistrationRecord record = mock(RegistrationRecord.class, Answers.RETURNS_DEEP_STUBS);
        // Course Code width is 12. This is 15 chars.
        when(record.getSection().getCourse().getCourseCode()).thenReturn("VERYLONGCOURSE!");
        when(record.getSection().getStartTime()).thenReturn(null);

        String row = formatter.formatRow(record);

        assertTrue(row.contains("VERYLONGC...")); // Trims to max width - 3 + "..."
    }
}