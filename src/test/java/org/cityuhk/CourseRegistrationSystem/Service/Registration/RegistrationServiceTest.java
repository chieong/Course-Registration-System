package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Observer.SectionVacancyObserver;
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

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private ArrayList<SectionVacancyObserver> observers;

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
        observers = new ArrayList<>();
        SectionVacancyObserver mockObserver = mock(SectionVacancyObserver.class);
        observers.add(mockObserver);
        registrationService.setObservers(observers);
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
        when(registrationRecordRepository.countEnrolled(sectionId)).thenReturn(1);
        when(section.isFull(1)).thenReturn(false);

        // Act
        registrationService.addSection(studentId, sectionId, timestamp);

        // Assert
        verify(registrationRecordRepository).save(any(RegistrationRecord.class));
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
    void addSection_SectionAlreadyFull() {
        Integer studentId = 1;
        Integer sectionId = 1;
        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(registrationRecordRepository.countEnrolled(sectionId)).thenReturn(1);
        when(section.isFull(1)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.addSection(studentId, sectionId, timestamp));
        assertEquals("Section is already full", exception.getMessage());

    }

    @Test
    void dropSection_Success() {
        Integer studentId = 1;
        Integer sectionId = 1;
        RegistrationRecord registrationRecord = mock(RegistrationRecord.class);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));

        List<Integer> eligibleCohorts = List.of(1);
        when(registrationPeriodRepository.getActiveCohortByTime(any(LocalDateTime.class))).thenReturn(eligibleCohorts);

        when(registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId)).thenReturn(Optional.of(registrationRecord));

        registrationService.dropSection(studentId, sectionId, timestamp);

        verify(registrationRecordRepository).delete(registrationRecord);

        verify(observers.get(0)).onVacancyOccurred(sectionId);
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
