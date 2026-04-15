package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionTests {

    @Test
    void testTimetableExportException() {
        TimetableExportException ex1 = new TimetableExportException("Export failed");
        assertEquals("Export failed", ex1.getMessage());

        Throwable cause = new RuntimeException("IO Error");
        TimetableExportException ex2 = new TimetableExportException("Export failed", cause);
        assertEquals(cause, ex2.getCause());
    }

    @Test
    void testTimetableValidationException() {
        TimetableValidationException ex1 = new TimetableValidationException("Validation failed");
        assertEquals("Validation failed", ex1.getMessage());

        Throwable cause = new RuntimeException("DB Error");
        TimetableValidationException ex2 = new TimetableValidationException("Validation failed", cause);
        assertEquals(cause, ex2.getCause());
    }
}