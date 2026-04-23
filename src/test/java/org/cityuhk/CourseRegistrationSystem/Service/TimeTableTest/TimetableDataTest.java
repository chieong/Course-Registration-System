package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TimetableData and Builder Tests")
class TimetableDataTest {

    private TimetableData.Builder builder;
    private Set<Section> mockSections;
    private Section mockSection;

    @BeforeEach
    void setUp() {
        builder = new TimetableData.Builder();
        mockSections = new HashSet<>();
        mockSection = mock(Section.class);
        mockSections.add(mockSection);
    }

    @Test
    @DisplayName("Should build TimetableData with required fields")
    void testBuildWithRequiredFields() {
        TimetableData data = builder
                .ownerId(123)
                .userType(TimetableData.UserType.Student)
                .sections(mockSections)
                .build();

        assertEquals(123, data.getOwnerId());
        assertEquals(TimetableData.UserType.Student, data.getUserType());
        assertEquals(mockSections, data.getSections());
    }

    @Test
    @DisplayName("Should build TimetableData with all fields")
    void testBuildWithAllFields() {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        TimetableData data = builder
                .ownerId(456)
                .ownerIdLabel("Staff ID")
                .userType(TimetableData.UserType.Instructor)
                .sections(mockSections)
                .dayFormatter(dayFormatter)
                .timeFormatter(timeFormatter)
                .build();

        assertEquals(456, data.getOwnerId());
        assertEquals("Staff ID", data.getOwnerIdLabel());
        assertEquals(TimetableData.UserType.Instructor, data.getUserType());
        assertEquals(mockSections, data.getSections());
        assertEquals(dayFormatter, data.getDayFormatter());
        assertEquals(timeFormatter, data.getTimeFormatter());
    }

    @Test
    @DisplayName("Should throw exception when ownerId is null")
    void testBuildWithNullOwnerId() {
        assertThrows(NullPointerException.class, () ->
                builder.ownerId(null)
        );
    }

    @Test
    @DisplayName("Should throw exception when ownerIdLabel is null")
    void testBuildWithNullOwnerIdLabel() {
        assertThrows(NullPointerException.class, () ->
                builder.ownerIdLabel(null)
        );
    }

    @Test
    @DisplayName("Should throw exception when dayFormatter is null")
    void testBuildWithNullDayFormatter() {
        assertThrows(NullPointerException.class, () ->
                builder.dayFormatter(null)
        );
    }

    @Test
    @DisplayName("Should throw exception when timeFormatter is null")
    void testBuildWithNullTimeFormatter() {
        assertThrows(NullPointerException.class, () ->
                builder.timeFormatter(null)
        );
    }

    @Test
    @DisplayName("Should throw IllegalStateException when building without ownerId")
    void testBuildWithoutOwnerId() {
        builder.userType(TimetableData.UserType.Student)
               .sections(mockSections);

        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when building without userType")
    void testBuildWithoutUserType() {
        builder.ownerId(123)
               .sections(mockSections);

        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    @DisplayName("Should use default formatters when not specified")
    void testDefaultFormatters() {
        TimetableData data = builder
                .ownerId(789)
                .userType(TimetableData.UserType.Student)
                .sections(mockSections)
                .build();

        assertNotNull(data.getDayFormatter());
        assertNotNull(data.getTimeFormatter());
    }

    @Test
    @DisplayName("Should use default ownerIdLabel when not specified")
    void testDefaultOwnerIdLabel() {
        TimetableData data = builder
                .ownerId(789)
                .userType(TimetableData.UserType.Student)
                .sections(mockSections)
                .build();

        assertEquals("Owner ID", data.getOwnerIdLabel());
    }

    @Test
    @DisplayName("Should generate correct toString representation")
    void testToString() {
        TimetableData data = builder
                .ownerId(123)
                .userType(TimetableData.UserType.Student)
                .sections(mockSections)
                .build();

        String result = data.toString();

        assertTrue(result.contains("ownerId=123"));
        assertTrue(result.contains("userType=Student"));
        assertTrue(result.contains("recordCount=1"));
    }

    @Test
    @DisplayName("Should return zero record count when sections is null")
    void testToStringWithNullSections() {
        TimetableData data = builder
                .ownerId(123)
                .userType(TimetableData.UserType.Student)
                .sections(null)
                .build();

        String result = data.toString();

        assertTrue(result.contains("recordCount=0"));
    }

    @Test
    @DisplayName("Should return zero record count when sections is empty")
    void testToStringWithEmptySections() {
        TimetableData data = builder
                .ownerId(123)
                .userType(TimetableData.UserType.Student)
                .sections(new HashSet<>())
                .build();

        String result = data.toString();

        assertTrue(result.contains("recordCount=0"));
    }

    @Test
    @DisplayName("Should get all properties correctly")
    void testGetters() {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        TimetableData data = builder
                .ownerId(999)
                .ownerIdLabel("Custom ID")
                .userType(TimetableData.UserType.Instructor)
                .sections(mockSections)
                .dayFormatter(dayFormatter)
                .timeFormatter(timeFormatter)
                .build();

        assertEquals(999, data.getOwnerId());
        assertEquals("Custom ID", data.getOwnerIdLabel());
        assertEquals(TimetableData.UserType.Instructor, data.getUserType());
        assertEquals(mockSections, data.getSections());
        assertEquals(dayFormatter, data.getDayFormatter());
        assertEquals(timeFormatter, data.getTimeFormatter());
    }
}