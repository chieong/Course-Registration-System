package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;



import org.cityuhk.CourseRegistrationSystem.Model.WaitlistRecord;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
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
    private StudentRepositoryPort studentRepository;

    @Mock
    private SectionRepositoryPort sectionRepository;

    @Mock
    private RegistrationRecordRepositoryPort registrationRecordRepository;

    @Mock
    private RegistrationPeriodRepository registrationPeriodRepository;

    @Mock
    private WaitlistRecordRepository waitlistRecordRepository;

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

    @Test
    void waitListSection_Success() {
        Integer studentId = 1;
        Integer sectionId = 1;
        WaitlistRecord record = mock(WaitlistRecord.class);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(eligibleCohorts);
        when(waitlistRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(waitlistRecordRepository.countWaitlisted(sectionId)).thenReturn(0);
        when(student.waitlistSection(section, timestamp, 0)).thenReturn(record);

        registrationService.waitListSection(studentId, sectionId, timestamp);

        verify(waitlistRecordRepository).save(record);
    }

    @Test
    void waitListSection_StudentNotFound() {
        when(studentRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(1, 1, timestamp));
        assertEquals("Student not found", ex.getMessage());
    }

    @Test
    void waitListSection_SectionNotFound() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(1, 1, timestamp));
        assertEquals("Section not found", ex.getMessage());
    }

    @Test
    void waitListSection_NotEligible() {
        when(student.getCohort()).thenReturn(99);
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class)))
                .thenReturn(eligibleCohorts);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(1, 1, timestamp));
        assertEquals("Student not eligible to register", ex.getMessage());
    }

    @Test
    void waitListSection_AlreadyWaitlisted() {
        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class)))
                .thenReturn(eligibleCohorts);
        when(waitlistRecordRepository.exists(1, 1)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(1, 1, timestamp));
        assertEquals("Already waitlisted", ex.getMessage());
    }
}
