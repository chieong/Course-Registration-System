package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Model.WaitlistRecord;
import org.cityuhk.CourseRegistrationSystem.Observer.SectionVacancyObserver;
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
    void waitlistSection_Success() {
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(Arrays.asList(1, 2, 3));
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);

        registrationService.waitListSection(studentId, sectionId, timestamp);
        verify(waitlistRecordRepository).save(any(WaitlistRecord.class));
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
    void waitListSection_SectionOverlappingWithExisting() {
        Integer studentId = 1;
        Integer sectionId = 1;

        RegistrationRecord registrationRecord = mock(RegistrationRecord.class);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);

        when(registrationRecordRepository.findByStudentId(studentId)).thenReturn(List.of(registrationRecord));
        when(registrationRecord.hasTimeConflictWith(section)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(studentId, sectionId, timestamp));

        assertEquals("Time conflict with existing section", exception.getMessage());
    }

    @Test
    void waitListSection_SectionOverlappingWithWaitlisted() {
        Integer studentId = 1;
        Integer sectionId = 1;

        RegistrationRecord registrationRecord = mock(RegistrationRecord.class);
        WaitlistRecord waitlistRecord = mock(WaitlistRecord.class);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(registrationRecordRepository.findByStudentId(studentId)).thenReturn(List.of(registrationRecord));
        when(registrationRecord.hasTimeConflictWith(section)).thenReturn(false);

        when(registrationRecordRepository.findByStudentId(studentId)).thenReturn(List.of(registrationRecord));
        when(registrationRecord.hasTimeConflictWith(section)).thenReturn(false);

        when(waitlistRecordRepository.findByStudentId(studentId)).thenReturn(List.of(waitlistRecord));
        when(waitlistRecord.hasTimeConflictWith(section)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.waitListSection(studentId, sectionId, timestamp));

        assertEquals("Time conflict with waitlisted section", exception.getMessage());
    }

    @Test
    void waitListSection_WaitlistFull() {
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(Arrays.asList(1, 2, 3));
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(waitlistRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(waitlistRecordRepository.countWaitlisted(sectionId)).thenReturn(1);
        when(section.isWaitlistFull(1)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.waitListSection(studentId,sectionId,timestamp));
        assertEquals("Waitlist is already full", exception.getMessage());
    }
    @Test
    void dropSection_Success() {
        Integer studentId = 1;
        Integer sectionId = 1;
        RegistrationRecord record = mock(RegistrationRecord.class);
        SectionVacancyObserver observer = mock(SectionVacancyObserver.class);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId)).thenReturn(Optional.of(record));

        registrationService.addObserver(observer);

        registrationService.dropSection(studentId, sectionId, timestamp);

        verify(registrationRecordRepository).delete(record);
        verify(observer).onVacancyOccurred(sectionId);
    }

    @Test
    void dropSection_StudentNotFound() {
        Integer studentId = 999;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, 1, timestamp));

        assertEquals("Student not found", exception.getMessage());
    }

    @Test
    void dropSection_SectionNotFound() {
        Integer studentId = 1;
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(anyInt())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, 999, timestamp));

        assertEquals("Section not found", exception.getMessage());
    }

    @Test
    void dropSection_StudentNotEligible() {
        Integer studentId = 1;
        List<Integer> ineligibleCohorts = Arrays.asList(9, 10);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(anyInt())).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(ineligibleCohorts);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, 1, timestamp));

        assertEquals("Student not eligible to register", exception.getMessage());
    }

    @Test
    void dropSection_StudentNotEligibleButAlreadyEnrolled_ShouldStillDrop() {
        Integer studentId = 1;
        Integer sectionId = 1;
        RegistrationRecord record = mock(RegistrationRecord.class);
        List<Integer> ineligibleCohorts = Arrays.asList(9, 10);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(ineligibleCohorts);
        when(registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId))
                .thenReturn(Optional.of(record));

        registrationService.dropSection(studentId, sectionId, timestamp);

        verify(registrationRecordRepository).delete(record);
    }

    @Test
    void dropSection_NotEnrolled() {
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.dropSection(studentId, sectionId, timestamp));

        assertEquals("Not enrolled", exception.getMessage());
    }
    @Test
    void dropWaitlist_Success() {
        Integer studentId = 1;
        Integer sectionId = 1;
        WaitlistRecord record = mock(WaitlistRecord.class);

        SectionVacancyObserver observer = mock(SectionVacancyObserver.class);
        when(observer.getStudentId()).thenReturn(studentId);
        registrationService.addObserver(observer);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(waitlistRecordRepository.findByStudentIdAndSectionId(studentId, sectionId)).thenReturn(Optional.of(record));

        registrationService.dropWaitlist(studentId, sectionId);

        verify(waitlistRecordRepository).delete(record);
    }

    @Test
    void dropWaitlist_StudentNotFound() {
        Integer studentId = 999;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.dropWaitlist(studentId, 1));

        assertEquals("Student not found", exception.getMessage());
    }

    @Test
    void dropWaitlist_StudentNotEligible() {
        Integer studentId = 1;
        List<Integer> ineligibleCohorts = Arrays.asList(5, 6);

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(ineligibleCohorts);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.dropWaitlist(studentId, 1));

        assertEquals("Student not eligible to register", exception.getMessage());
    }

    @Test
    void dropWaitlist_NotWaitlisted() {
        Integer studentId = 1;
        Integer sectionId = 1;

        when(student.getCohort()).thenReturn(1);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.getActiveCohortByTime(any())).thenReturn(eligibleCohorts);
        when(waitlistRecordRepository.findByStudentIdAndSectionId(studentId, sectionId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.dropWaitlist(studentId, sectionId));

        assertEquals("Not waitlisted", exception.getMessage());
    }
}
