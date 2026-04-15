package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TimetableDataTest {

    @Test
    void build_WithValidData_CreatesInstance() {
        List<RegistrationRecord> records = List.of(mock(RegistrationRecord.class));
        DateTimeFormatter customDayFormatter = DateTimeFormatter.ofPattern("EEEE");

        TimetableData data = new TimetableData.Builder()
                .studentId(12345678)
                .registrationRecords(records)
                .dayFormatter(customDayFormatter)
                .build();

        assertEquals(12345678, data.getStudentId());
        assertEquals(1, data.getRegistrationRecords().size());
        assertEquals(customDayFormatter, data.getDayFormatter());
        assertNotNull(data.getTimeFormatter()); // Should fall back to default
        assertTrue(data.toString().contains("12345678"));
    }

    @Test
    void build_MissingStudentId_ThrowsException() {
        List<RegistrationRecord> records = List.of(mock(RegistrationRecord.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                new TimetableData.Builder()
                        .registrationRecords(records)
                        .build()
        );
        assertEquals("Student ID is required", exception.getMessage());
    }

    @Test
    void build_EmptyRegistrationRecords_ThrowsException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                new TimetableData.Builder()
                        .studentId(12345678)
                        .registrationRecords(Collections.emptyList())
                        .build()
        );
        assertEquals("Registration records cannot be empty", exception.getMessage());
    }

    @Test
    void builder_NullSetters_ThrowsNullPointerException() {
        TimetableData.Builder builder = new TimetableData.Builder();

        assertThrows(NullPointerException.class, () -> builder.studentId(null));
        assertThrows(NullPointerException.class, () -> builder.registrationRecords(null));
        assertThrows(NullPointerException.class, () -> builder.dayFormatter(null));
        assertThrows(NullPointerException.class, () -> builder.timeFormatter(null));
    }
}