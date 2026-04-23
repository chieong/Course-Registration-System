package org.cityuhk.CourseRegistrationSystem.Service.TimeTableTest;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidationException;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TimetableValidator Tests")
class TimetableValidatorTest {

    private TimetableValidator validator;
    private StudentRepositoryPort studentRepository;
    private InstructorRepositoryPort instructorRepository;
    private RegistrationRecordRepositoryPort registrationRecordRepository;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepositoryPort.class);
        instructorRepository = mock(InstructorRepositoryPort.class);
        registrationRecordRepository = mock(RegistrationRecordRepositoryPort.class);

        validator = new TimetableValidator(studentRepository, instructorRepository, registrationRecordRepository);
    }

    // Tests for validateStudentForExport
    @Test
    @DisplayName("Should validate student successfully when data is valid")
    void testValidateStudentSuccess() throws TimetableValidationException {
        Student mockStudent = mock(Student.class);
        when(studentRepository.findById(123)).thenReturn(Optional.of(mockStudent));
        when(registrationRecordRepository.findByStudentId(123)).thenReturn(List.of(mock(RegistrationRecord.class)));

        Student result = validator.validateStudentForExport(123);

        assertEquals(mockStudent, result);
    }

    @Test
    @DisplayName("Should throw exception when student ID is null")
    void testValidateStudentWithNullId() {
        assertThrows(TimetableValidationException.class, () ->
                validator.validateStudentForExport(null)
        );
    }

    @Test
    @DisplayName("Should throw exception when student ID is zero")
    void testValidateStudentWithZeroId() {
        assertThrows(TimetableValidationException.class, () ->
                validator.validateStudentForExport(0)
        );
    }

    @Test
    @DisplayName("Should throw exception when student ID is negative")
    void testValidateStudentWithNegativeId() {
        assertThrows(TimetableValidationException.class, () ->
                validator.validateStudentForExport(-1)
        );
    }

    @Test
    @DisplayName("Should throw exception when student not found")
    void testValidateStudentNotFound() {
        when(studentRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(TimetableValidationException.class, () ->
                validator.validateStudentForExport(999)
        );
    }

    @Test
    @DisplayName("Should throw exception when student has no registration records")
    void testValidateStudentWithNoRecords() {
        Student mockStudent = mock(Student.class);
        when(studentRepository.findById(123)).thenReturn(Optional.of(mockStudent));
        when(registrationRecordRepository.findByStudentId(123)).thenReturn(Collections.emptyList());

        assertThrows(TimetableValidationException.class, () ->
                validator.validateStudentForExport(123)
        );
    }

    @Test
    @DisplayName("Should throw exception when registration records are null")
    void testValidateStudentWithNullRecords() {
        Student mockStudent = mock(Student.class);
        when(studentRepository.findById(123)).thenReturn(Optional.of(mockStudent));
        when(registrationRecordRepository.findByStudentId(123)).thenReturn(null);

        assertThrows(TimetableValidationException.class, () ->
                validator.validateStudentForExport(123)
        );
    }

    // Tests for validateInstructorForExport
    @Test
    @DisplayName("Should validate instructor successfully when data is valid")
    void testValidateInstructorSuccess() throws TimetableValidationException {
        Instructor mockInstructor = mock(Instructor.class);
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));
        when(mockInstructor.getSections()).thenReturn(sections);
        when(instructorRepository.findById(456)).thenReturn(Optional.of(mockInstructor));

        Instructor result = validator.validateInstructorForExport(456);

        assertEquals(mockInstructor, result);
    }

    @Test
    @DisplayName("Should throw exception when staff ID is null")
    void testValidateInstructorWithNullId() {
        assertThrows(TimetableValidationException.class, () ->
                validator.validateInstructorForExport(null)
        );
    }

    @Test
    @DisplayName("Should throw exception when staff ID is zero")
    void testValidateInstructorWithZeroId() {
        assertThrows(TimetableValidationException.class, () ->
                validator.validateInstructorForExport(0)
        );
    }

    @Test
    @DisplayName("Should throw exception when staff ID is negative")
    void testValidateInstructorWithNegativeId() {
        assertThrows(TimetableValidationException.class, () ->
                validator.validateInstructorForExport(-100)
        );
    }

    @Test
    @DisplayName("Should throw exception when instructor not found")
    void testValidateInstructorNotFound() {
        when(instructorRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(TimetableValidationException.class, () ->
                validator.validateInstructorForExport(999)
        );
    }

    @Test
    @DisplayName("Should throw exception when instructor has no sections")
    void testValidateInstructorWithNoSections() {
        Instructor mockInstructor = mock(Instructor.class);
        when(mockInstructor.getSections()).thenReturn(new HashSet<>());
        when(instructorRepository.findById(456)).thenReturn(Optional.of(mockInstructor));

        assertThrows(TimetableValidationException.class, () ->
                validator.validateInstructorForExport(456)
        );
    }

    @Test
    @DisplayName("Should throw exception when sections are null")
    void testValidateInstructorWithNullSections() {
        Instructor mockInstructor = mock(Instructor.class);
        when(mockInstructor.getSections()).thenReturn(null);
        when(instructorRepository.findById(456)).thenReturn(Optional.of(mockInstructor));

        assertThrows(TimetableValidationException.class, () ->
                validator.validateInstructorForExport(456)
        );
    }

    // Tests for validateTimetableData
    @Test
    @DisplayName("Should validate timetable data successfully")
    void testValidateTimetableDataSuccess() throws TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));

        TimetableData data = new TimetableData.Builder()
                .ownerId(123)
                .userType(TimetableData.UserType.Student)
                .sections(sections)
                .build();

        assertDoesNotThrow(() -> validator.validateTimetableData(data));
    }

    @Test
    @DisplayName("Should throw exception when timetable data is null")
    void testValidateTimetableDataNull() {
        assertThrows(TimetableValidationException.class, () ->
                validator.validateTimetableData(null)
        );
    }

    @Test
    @DisplayName("Should throw exception when owner ID is null")
    void testValidateTimetableDataWithNullOwnerId() {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));

        TimetableData data = mock(TimetableData.class);
        when(data.getOwnerId()).thenReturn(null);
        when(data.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(data.getSections()).thenReturn(sections);

        assertThrows(TimetableValidationException.class, () ->
                validator.validateTimetableData(data)
        );
    }

    @Test
    @DisplayName("Should throw exception when user type is null")
    void testValidateTimetableDataWithNullUserType() {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));

        TimetableData data = mock(TimetableData.class);
        when(data.getOwnerId()).thenReturn(123);
        when(data.getUserType()).thenReturn(null);
        when(data.getSections()).thenReturn(sections);

        assertThrows(TimetableValidationException.class, () ->
                validator.validateTimetableData(data)
        );
    }

    @Test
    @DisplayName("Should throw exception when sections are null")
    void testValidateTimetableDataWithNullSections() {
        TimetableData data = mock(TimetableData.class);
        when(data.getOwnerId()).thenReturn(123);
        when(data.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(data.getSections()).thenReturn(null);
        when(data.getDayFormatter()).thenReturn(mock(java.time.format.DateTimeFormatter.class));
        when(data.getTimeFormatter()).thenReturn(mock(java.time.format.DateTimeFormatter.class));

        assertThrows(TimetableValidationException.class, () ->
                validator.validateTimetableData(data)
        );
    }

    @Test
    @DisplayName("Should throw exception when sections are empty")
    void testValidateTimetableDataWithEmptySections() {
        TimetableData data = mock(TimetableData.class);
        when(data.getOwnerId()).thenReturn(123);
        when(data.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(data.getSections()).thenReturn(new HashSet<>());
        when(data.getDayFormatter()).thenReturn(mock(java.time.format.DateTimeFormatter.class));
        when(data.getTimeFormatter()).thenReturn(mock(java.time.format.DateTimeFormatter.class));

        assertThrows(TimetableValidationException.class, () ->
                validator.validateTimetableData(data)
        );
    }

    @Test
    @DisplayName("Should throw exception when day formatter is null")
    void testValidateTimetableDataWithNullDayFormatter() {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));

        TimetableData data = mock(TimetableData.class);
        when(data.getOwnerId()).thenReturn(123);
        when(data.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(data.getSections()).thenReturn(sections);
        when(data.getDayFormatter()).thenReturn(null);
        when(data.getTimeFormatter()).thenReturn(mock(java.time.format.DateTimeFormatter.class));

        assertThrows(TimetableValidationException.class, () ->
                validator.validateTimetableData(data)
        );
    }

    @Test
    @DisplayName("Should throw exception when time formatter is null")
    void testValidateTimetableDataWithNullTimeFormatter() {
        Set<Section> sections = new HashSet<>();
        sections.add(mock(Section.class));

        TimetableData data = mock(TimetableData.class);
        when(data.getOwnerId()).thenReturn(123);
        when(data.getUserType()).thenReturn(TimetableData.UserType.Student);
        when(data.getSections()).thenReturn(sections);
        when(data.getDayFormatter()).thenReturn(mock(java.time.format.DateTimeFormatter.class));
        when(data.getTimeFormatter()).thenReturn(null);

        assertThrows(TimetableValidationException.class, () ->
                validator.validateTimetableData(data)
        );
    }
}