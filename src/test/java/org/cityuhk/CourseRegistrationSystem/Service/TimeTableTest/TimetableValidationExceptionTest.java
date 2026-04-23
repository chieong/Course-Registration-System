package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimetableValidationException Tests")
class TimetableValidationExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void testExceptionWithMessage() {
        String message = "Invalid student ID";
        TimetableValidationException exception = new TimetableValidationException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testExceptionWithMessageAndCause() {
        String message = "Validation failed";
        Throwable cause = new IllegalArgumentException("Invalid ID");
        TimetableValidationException exception = new TimetableValidationException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should be instance of Exception")
    void testIsInstanceOfException() {
        TimetableValidationException exception = new TimetableValidationException("Test");

        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Should support exception chaining")
    void testExceptionChaining() {
        Throwable rootCause = new NullPointerException("Null value");
        TimetableValidationException exception = new TimetableValidationException("Validation error", rootCause);

        assertThrows(TimetableValidationException.class, () -> {
            throw exception;
        });
    }
}