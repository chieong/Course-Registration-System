package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Model.WaitlistRecord;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WaitlistServiceTest {

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
    void waitListSection_Success() {
        Integer studentId = 1;
        Integer sectionId = 1;
        WaitlistRecord waitlistRecord = mock(WaitlistRecord.class);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(waitlistRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(waitlistRecordRepository.countWaitlisted(sectionId)).thenReturn(0);
        when(student.waitlistSection(any(), any(), anyInt())).thenReturn(waitlistRecord);

        registrationService.waitListSection(studentId, sectionId, timestamp);

        verify(waitlistRecordRepository).save(waitlistRecord);
    }

    @Test
    void waitListSection_StudentNotFound() {
        Integer studentId = 999;
        Integer sectionId = 1;

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(studentId, sectionId, timestamp));

        assertEquals("Student not found", exception.getMessage());
    }

    @Test
    void waitListSection_SectionNotFound() {
        Integer studentId = 1;
        Integer sectionId = 999;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(studentId, sectionId, timestamp));

        assertEquals("Section not found", exception.getMessage());
    }

    @Test
    void waitListSection_StudentNotEligible() {
        Integer studentId = 1;
        Integer sectionId = 1;
        List<Integer> ineligibleCohorts = Arrays.asList(4, 5, 6);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(ineligibleCohorts);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(studentId, sectionId, timestamp));

        assertEquals("Student not eligible to register", exception.getMessage());
    }

    @Test
    void waitListSection_AlreadyWaitlisted() {
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(waitlistRecordRepository.exists(studentId, sectionId)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(studentId, sectionId, timestamp));

        assertEquals("Already waitlisted", exception.getMessage());
    }

    @Test
    @org.junit.jupiter.api.Disabled("Implementation missing: check if student already enrolled before waitlisting")
    // TODO: Implementation missing: check if student already enrolled before waitlisting
    void waitListSection_AlreadyEnrolled() {
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(studentId, sectionId, timestamp));

        assertEquals("Already enrolled", exception.getMessage());
    }

    @Test
    @org.junit.jupiter.api.Disabled("Implementation missing: check waitlist capacity before adding")
    // TODO: Implementation missing: check waitlist capacity before adding
    void waitListSection_WaitlistFull() {
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(waitlistRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(waitlistRecordRepository.countWaitlisted(sectionId)).thenReturn(5);
        when(section.isWaitlistFull(5)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(studentId, sectionId, timestamp));

        assertEquals("Waitlist is full", exception.getMessage());
    }
}
