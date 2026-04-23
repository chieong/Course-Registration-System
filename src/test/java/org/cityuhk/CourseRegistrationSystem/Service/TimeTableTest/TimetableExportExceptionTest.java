package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableExportException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimetableExportException Tests")
class TimetableExportExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void testExceptionWithMessage() {
        String message = "Export failed";
        TimetableExportException exception = new TimetableExportException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testExceptionWithMessageAndCause() {
        String message = "Export failed";
        Throwable cause = new RuntimeException("IO Error");
        TimetableExportException exception = new TimetableExportException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should be instance of Exception")
    void testIsInstanceOfException() {
        TimetableExportException exception = new TimetableExportException("Test");

        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Should preserve cause chain")
    void testCauseChain() {
        Throwable rootCause = new RuntimeException("Root cause");
        TimetableExportException exception = new TimetableExportException("Export error", rootCause);

        assertThrows(TimetableExportException.class, () -> {
            throw exception;
        });
    }
}