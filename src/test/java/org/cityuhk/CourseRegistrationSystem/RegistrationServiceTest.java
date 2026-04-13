package org.cityuhk.CourseRegistrationSystem;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Service.RegistrationService;
import org.cityuhk.CourseRegistrationSystem.Service.Semester;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrationServiceTest {

    private Semester createTestSemester() {
        LocalDateTime now = LocalDateTime.now();
        return new Semester(now, now.plusDays(120));
    }

    @Test
    void addSectionThrowsWhenStudentNotFoundTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        when(studentRepo.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addSection(1, 10, LocalDateTime.now(), createTestSemester()));
        assertTrue(ex.getMessage().contains("Student not found"));
        verify(studentRepo).findById(1);
    }

    @Test
    void addSectionThrowsWhenSectionNotFoundTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        Student student = new Student.StudentBuilder()
                .withUserEID("s001")
                .withName("Test Student")
                .withStudentId(1)
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(999)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(999)
                .build();

        when(studentRepo.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepo.findById(10)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addSection(1, 10, LocalDateTime.now(), createTestSemester()));
        assertTrue(ex.getMessage().contains("Section not found"));
        verify(sectionRepo).findById(10);
    }

    @Test
    void addSectionThrowsWhenAlreadyEnrolledTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        Student student = new Student.StudentBuilder()
                .withUserEID("s001")
                .withName("Test Student")
                .withStudentId(1)
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(999)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(999)
                .build();
        Section section = new Section();
        LocalDateTime timestamp = LocalDateTime.now();

        when(studentRepo.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepo.findById(10)).thenReturn(Optional.of(section));
        when(recordRepo.exists(1, 10, timestamp)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addSection(1, 10, timestamp, createTestSemester()));
        assertTrue(ex.getMessage().contains("Already enrolled"));
        verify(recordRepo).exists(1, 10, timestamp);
    }

    @Test
    void addSectionCallsCountEnrolledWhenAllValidTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        Student student = new Student.StudentBuilder()
                .withUserEID("s001")
                .withName("Test Student")
                .withStudentId(1)
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(999)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(999)
                .build();
        Section section = new Section();
        LocalDateTime timestamp = LocalDateTime.now();

        when(studentRepo.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepo.findById(10)).thenReturn(Optional.of(section));
        when(recordRepo.exists(1, 10, timestamp)).thenReturn(false);
        when(recordRepo.countEnrolled(10)).thenReturn(5);

        try {
            service.addSection(1, 10, timestamp, createTestSemester());
        } catch (Exception e) {
            // Expected if section.canEnroll() fails, but we verify countEnrolled was called
        }
        
        verify(recordRepo).countEnrolled(10);
    }

    @Test
    void deleteStudentCallsDeleteByIdTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        service.deleteStudent(1);

        verify(studentRepo).deleteById(1);
    }

    @Test
    void addSectionCallsSaveWhenAllSuccessfulTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        // Create a real Student (not mocked) that we can call addSection on
        Student student = new Student.StudentBuilder()
                .withUserEID("s001")
                .withName("Test Student")
                .withStudentId(1)
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(999)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(999)
                .build();

        // Mock Section to return true for canEnroll, so student.addSection() succeeds
        Section section = mock(Section.class);
        when(section.canEnroll(student, 5)).thenReturn(true);
        
        LocalDateTime timestamp = LocalDateTime.now();

        when(studentRepo.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepo.findById(10)).thenReturn(Optional.of(section));
        when(recordRepo.exists(1, 10, timestamp)).thenReturn(false);
        when(recordRepo.countEnrolled(10)).thenReturn(5);

        service.addSection(1, 10, timestamp, createTestSemester());

        // Verify save() was called with any RegistrationRecord
        verify(recordRepo).save(any(RegistrationRecord.class));
    }
}
