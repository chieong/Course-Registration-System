package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private RegistrationRecordRepository registrationRecordRepository;

    @Mock
    private RegistrationPeriodRepository registrationPeriodRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private Student student;
    private Section section;
    private LocalDateTime timestamp;
    private List<Integer> eligibleCohorts;

    @BeforeEach
    void setUp() {
        student = mock(Student.class);
        section = mock(Section.class);
        timestamp = LocalDateTime.now();
        eligibleCohorts = Arrays.asList(1, 2, 3);
    }

    @Test
    void addSection_Success() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;
        RegistrationRecord registrationRecord = mock(RegistrationRecord.class);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(registrationRecordRepository.countEnrolled(sectionId)).thenReturn(5);
        when(student.addSection(section, timestamp, 5)).thenReturn(registrationRecord);

        // Act
        registrationService.addSection(studentId, sectionId, timestamp);

        // Assert
        verify(registrationRecordRepository).save(registrationRecord);
    }

    @Test
    void addSection_StudentNotFound() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.addSection(studentId, sectionId, timestamp));
        assertEquals("Student not found", exception.getMessage());
    }

    @Test
    void addSection_SectionNotFound() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.addSection(studentId, sectionId, timestamp));
        assertEquals("Section not found", exception.getMessage());
    }

    @Test
    void addSection_StudentNotEligible() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;
        List<Integer> ineligibleCohorts = Arrays.asList(2, 3, 4);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(ineligibleCohorts);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.addSection(studentId, sectionId, timestamp));
        assertEquals("Student not eligible to register", exception.getMessage());
    }

    @Test
    void addSection_AlreadyEnrolled() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.addSection(studentId, sectionId, timestamp));
        assertEquals("Already enrolled", exception.getMessage());
    }

    @Test
    void dropSection_Success() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;
        RegistrationRecord registrationRecord = mock(RegistrationRecord.class);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId)).thenReturn(Optional.of(registrationRecord));

        // Act
        registrationService.dropSection(studentId, sectionId, timestamp);

        // Assert
        verify(registrationRecordRepository).delete(registrationRecord);
    }

    @Test
    void dropSection_StudentNotFound() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, sectionId, timestamp));
        assertEquals("Student not found", exception.getMessage());
    }

    @Test
    void dropSection_SectionNotFound() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, sectionId, timestamp));
        assertEquals("Section not found", exception.getMessage());
    }

    @Test
    void dropSection_StudentNotEligible() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;
        List<Integer> ineligibleCohorts = Arrays.asList(2, 3, 4);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(ineligibleCohorts);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, sectionId, timestamp));
        assertEquals("Student not eligible to register", exception.getMessage());
    }

    @Test
    void dropSection_NotEnrolled() {
        // Arrange
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, sectionId, timestamp));
        assertEquals("Not enrolled", exception.getMessage());
    }
}