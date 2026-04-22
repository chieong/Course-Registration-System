package org.cityuhk.CourseRegistrationSystem.Observer;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Model.WaitlistRecord;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WaitlistManagerTest {

    @Mock
    private WaitlistRecordRepository waitlistRepository;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private Student student;

    private WaitlistManager waitlistManager;

    private WaitlistRecord waitlistRecord;
    private Integer sectionId;
    private Integer studentId;

    @BeforeEach
    void setUp() {
        waitlistManager = new WaitlistManager(waitlistRepository, registrationService,10);
        sectionId = 1;
        studentId = 10;
        waitlistRecord = mock(WaitlistRecord.class);
    }

    @Test
    void onVacancyOccurred_EnrollsFirstInWaitlist() {
        when(waitlistRepository.findFirstBySectionIdOrderByTimestampAsc(sectionId))
                .thenReturn(Optional.of(waitlistRecord));
        when(waitlistRecord.getStudent()).thenReturn(student);
        when(student.getStudentId()).thenReturn(studentId);

        waitlistManager.onVacancyOccurred(sectionId);

        verify(registrationService).addSection(eq(studentId), eq(sectionId), any(LocalDateTime.class));
    }

    @Test
    void onVacancyOccurred_DeletesWaitlistRecord() {
        when(waitlistRepository.findFirstBySectionIdOrderByTimestampAsc(sectionId))
                .thenReturn(Optional.of(waitlistRecord));
        when(waitlistRecord.getStudent()).thenReturn(student);

        waitlistManager.onVacancyOccurred(sectionId);

        verify(waitlistRepository).delete(waitlistRecord);
    }

    @Test
    void onVacancyOccurred_NoWaitlistedStudents() {
        when(waitlistRepository.findFirstBySectionIdOrderByTimestampAsc(sectionId))
                .thenReturn(Optional.empty());

        waitlistManager.onVacancyOccurred(sectionId);

        verify(registrationService, never()).addSection(anyInt(), anyInt(), any(LocalDateTime.class));
        verify(waitlistRepository, never()).delete(any(WaitlistRecord.class));
    }

    @Test
    void onVacancyOccurred_AddSectionFails() {
        when(waitlistRepository.findFirstBySectionIdOrderByTimestampAsc(sectionId))
                .thenReturn(Optional.of(waitlistRecord));
        when(waitlistRecord.getStudent()).thenReturn(student);
        when(student.getStudentId()).thenReturn(studentId);
        doThrow(new RuntimeException("Registration failed"))
                .when(registrationService).addSection(anyInt(), anyInt(), any(LocalDateTime.class));

        waitlistManager.onVacancyOccurred(sectionId);

        verify(waitlistRepository, never()).delete(any(WaitlistRecord.class));
    }
}