package org.cityuhk.CourseRegistrationSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Service.RegistrationService;
import org.cityuhk.CourseRegistrationSystem.Service.Semester;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
        when(recordRepo.exists(1, 10)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addSection(1, 10, timestamp, createTestSemester()));
        assertTrue(ex.getMessage().contains("Already enrolled"));
        verify(recordRepo).exists(1, 10);
    }

    @Test
    void ExportTimeTableThrowsWhenStudentNotFoundTest(){
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        when(studentRepo.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.ExportTimeTable(1));
        assertTrue(ex.getMessage().contains("Student not found"));
        verify(studentRepo).findById(1);
    }

    @Test 
    void ExportTimeTableThrowsWhenFailedToExportTest(){
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
        when(recordRepo.findByStudentId(1)).thenReturn(Collections.emptyList());

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createTempFile("student-1-timetable-", ".txt"))
                     .thenThrow(new IOException("Disk full"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.ExportTimeTable(1));

            assertTrue(ex.getMessage().contains("Failed to export timetable"));
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause() instanceof IOException);
        }
    }

    @Test
    void ExportTimeTableSuccessTest() throws IOException {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        Integer studentId = 1;
        Student student = new Student.StudentBuilder()
                .withUserEID("s001")
                .withName("Test Student")
                .withStudentId(studentId)
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(999)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(999)
                .build();

        RegistrationRecord record = mock(RegistrationRecord.class);
        String row = "MON    10:00        CS101        A1       LEC               Y1234";
        when(record.toTimetableRow(any(), any())).thenReturn(row);

        // optional but safe for Collections.sort(records)
        when(record.compareTo(any(RegistrationRecord.class))).thenReturn(0);

        List<RegistrationRecord> records = new ArrayList<>();
        records.add(record);

        when(studentRepo.findById(studentId)).thenReturn(Optional.of(student));
        when(recordRepo.findByStudentId(studentId)).thenReturn(records);

        Path output = service.ExportTimeTable(studentId);

        assertNotNull(output);
        assertTrue(Files.exists(output));

        String content = Files.readString(output);
        assertTrue(content.contains("STUDENT TIMETABLE"));
        assertTrue(content.contains("Student ID: 1"));
        assertTrue(content.contains(row)); // confirms writer.write(row) happened

        verify(record).toTimetableRow(any(), any()); // confirms loop line executed

        Files.deleteIfExists(output);
    }
}
