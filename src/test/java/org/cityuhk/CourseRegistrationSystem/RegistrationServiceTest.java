package org.cityuhk.CourseRegistrationSystem;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class RegistrationServiceTest {

    @Test
    void addSectionThrowsWhenStudentNotFoundTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        when(studentRepo.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex =
                assertThrows(
                        RuntimeException.class,
                        () -> service.addSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Student not found"));
        verify(studentRepo).findById(1);
    }

    @Test
    void addSectionThrowsWhenSectionNotFoundTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        Student student =
                new Student.StudentBuilder()
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

        RuntimeException ex =
                assertThrows(
                        RuntimeException.class,
                        () -> service.addSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Section not found"));
        verify(sectionRepo).findById(10);
    }

    @Test
    void addSectionThrowsWhenAlreadyEnrolledTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        Student student =
                new Student.StudentBuilder()
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

        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> service.addSection(1, 10, timestamp));
        assertTrue(ex.getMessage().contains("Already enrolled"));
        verify(recordRepo).exists(1, 10);
    }

    @Test
    void addSectionCallsCountEnrolledWhenAllValidTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        Student student =
                new Student.StudentBuilder()
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
        when(recordRepo.exists(1, 10)).thenReturn(false);
        when(recordRepo.countEnrolled(10)).thenReturn(5);

        try {
            service.addSection(1, 10, timestamp);
        } catch (Exception e) {
            // Expected if section.canEnroll() fails, but we verify countEnrolled was called
        }

        verify(recordRepo).countEnrolled(10);
    }

    // @Test
    // void addSectionCallsSaveWhenAllSuccessfulTest() {
    //     StudentRepository studentRepo = mock(StudentRepository.class);
    //     SectionRepository sectionRepo = mock(SectionRepository.class);
    //     RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
    //     RegistrationService service = new RegistrationService(studentRepo, sectionRepo,
    // recordRepo);

    //     // Create a real Student (not mocked) that we can call addSection on
    //     Student student = new Student.StudentBuilder()
    //             .withUserEID("s001")
    //             .withName("Test Student")
    //             .withStudentId(1)
    //             .withMinSemesterCredit(0)
    //             .withMaxSemesterCredit(999)
    //             .withMajor("CS")
    //             .withCohort(2024)
    //             .withDepartment("CS")
    //             .withMaxDegreeCredit(999)
    //             .build();

    //     // Mock Section to return true for canEnroll, so student.addSection() succeeds
    //     Section section = mock(Section.class);
    //     when(section.canEnroll(student, 5)).thenReturn(true);

    //     LocalDateTime timestamp = LocalDateTime.now();

    //     when(studentRepo.findById(1)).thenReturn(Optional.of(student));
    //     when(sectionRepo.findById(10)).thenReturn(Optional.of(section));
    //     when(recordRepo.exists(1, 10, timestamp)).thenReturn(false);
    //     when(recordRepo.countEnrolled(10)).thenReturn(5);

    //     service.addSection(1, 10, timestamp, createTestSemester());

    //     // Verify save() was called with any RegistrationRecord
    //     verify(recordRepo).save(any(RegistrationRecord.class));

    // }

    @Test
    void addSectionCallsSaveWhenAllSuccessful_withoutMockito() {
        Student student =
                new Student.StudentBuilder()
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

        // Force enrollment check to pass
        Section section =
                new Section() {
                    @Override
                    public boolean canEnroll(Student s, int enrolled) {
                        return true;
                    }
                };

        LocalDateTime timestamp = LocalDateTime.now();

        AtomicBoolean saveCalled = new AtomicBoolean(false);
        AtomicReference<RegistrationRecord> savedRecord = new AtomicReference<>();

        // Minimal port-interface stubs (avoid JpaRepository duplicate-save bytecode clash)
        StudentRepositoryPort stubstudentrepo = new StudentRepositoryPort() {
            @Override public Optional<Student> findByUserEID(String userEID) {
                throw new UnsupportedOperationException();
            }
            @Override public Optional<Student> findById(Integer id) {
                return Optional.of(student);
            }
        };

        SectionRepositoryPort stubsectionRepo = new SectionRepositoryPort() {
            @Override public Optional<Section> findById(Integer id) { return Optional.of(section); }
            @Override public Section save(Section s) { throw new UnsupportedOperationException(); }
            @Override public void deleteById(Integer id) { throw new UnsupportedOperationException(); }
            @Override public List<Section> findAll() { throw new UnsupportedOperationException(); }
        };

        RegistrationRecordRepositoryPort stubrecordRepo = new RegistrationRecordRepositoryPort() {
            @Override public int countEnrolled(Integer sectionId) { return 5; }
            @Override public List<RegistrationRecord> findAllRecords() { throw new UnsupportedOperationException(); }
            @Override public boolean exists(Integer studentId, Integer sectionId) { return false; }
            @Override public Optional<RegistrationRecord> findByStudentIdAndSectionId(Integer s, Integer sec) { return Optional.empty(); }
            @Override public List<RegistrationRecord> find(Integer studentId, LocalDateTime start, LocalDateTime end) { throw new UnsupportedOperationException(); }
            @Override public List<RegistrationRecord> findByStudentId(Integer studentId) { throw new UnsupportedOperationException(); }
            @Override public RegistrationRecord save(RegistrationRecord record) {
                saveCalled.set(true);
                savedRecord.set(record);
                return record;
            }
            @Override public void delete(RegistrationRecord record) { throw new UnsupportedOperationException(); }
        };

        // StudentRepository studentRepo = (StudentRepository) Proxy.newProxyInstance(
        //         StudentRepository.class.getClassLoader(),
        //         new Class<?>[] { StudentRepository.class },
        //         (proxy, method, args) -> {
        //             if (method.getName().equals("findById")) return Optional.of(student);
        //             if (method.getName().equals("toString")) return "StudentRepoStub";
        //             if (method.getName().equals("hashCode")) return
        // System.identityHashCode(proxy);
        //             if (method.getName().equals("equals")) return proxy == args[0];
        //             throw new UnsupportedOperationException("Not stubbed: " + method.getName());
        //         });

        // SectionRepository sectionRepo = (SectionRepository) Proxy.newProxyInstance(
        //         SectionRepository.class.getClassLoader(),
        //         new Class<?>[] { SectionRepository.class },
        //         (proxy, method, args) -> {
        //             if (method.getName().equals("findById")) return Optional.of(section);
        //             if (method.getName().equals("toString")) return "SectionRepoStub";
        //             if (method.getName().equals("hashCode")) return
        // System.identityHashCode(proxy);
        //             if (method.getName().equals("equals")) return proxy == args[0];
        //             throw new UnsupportedOperationException("Not stubbed: " + method.getName());
        //         });

        // RegistrationRecordRepository recordRepo = (RegistrationRecordRepository)
        // Proxy.newProxyInstance(
        //         RegistrationRecordRepository.class.getClassLoader(),
        //         new Class<?>[] { RegistrationRecordRepository.class },
        //         (proxy, method, args) -> {
        //             if (method.getName().equals("exists")) return false;
        //             if (method.getName().equals("countEnrolled")) return 5;
        //             if (method.getName().equals("save")) {
        //                 saveCalled.set(true);
        //                 savedRecord.set((RegistrationRecord) args[0]);
        //                 return args[0];
        //             }
        //             if (method.getName().equals("toString")) return "RecordRepoStub";
        //             if (method.getName().equals("hashCode")) return
        // System.identityHashCode(proxy);
        //             if (method.getName().equals("equals")) return proxy == args[0];
        //             throw new UnsupportedOperationException("Not stubbed: " + method.getName());
        //         });

        RegistrationService service = new RegistrationService(stubstudentrepo, stubsectionRepo, stubrecordRepo);

        service.addSection(1, 10, timestamp);

        assertTrue(saveCalled.get(), "Expected save(...) to be called");
        assertNotNull(savedRecord.get(), "Expected saved RegistrationRecord");
    }

    // NOTE: Export timetable tests have been moved to TimetableServiceTests
    // in org.cityuhk.CourseRegistrationSystem.Service.Timetable package
    // The export functionality is now handled by TimetableService following
    // SOLID principles and GoF patterns (Facade, Strategy, Builder patterns)

    @Test
    void dropSectionThrowsWhenStudentNotFoundTest() {
        StudentRepository studentRepo = mock(StudentRepository.class);
        SectionRepository sectionRepo = mock(SectionRepository.class);
        RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.dropSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Student not found"));
    }

    @Test
    void dropSectionThrowsWhenSectionNotFoundTest() {
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
                () -> service.dropSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Section not found"));
        verify(sectionRepo).findById(10);
    }

    @Test
    void dropSectionThrowsWhenNotEnrolledTest() {
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

        Section section = mock(Section.class);
        
        when(studentRepo.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepo.findById(10)).thenReturn(Optional.of(section));
        when(recordRepo.findByStudentIdAndSectionId(1, 10)).thenReturn(Optional.empty());
        
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.dropSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Not enrolled"));
        verify(recordRepo).findByStudentIdAndSectionId(1, 10);
    }

    @Test
    void dropSectionCallsDeleteWhenSuccessfulTest() {
        LocalDateTime timestamp = LocalDateTime.now();
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

        Section section = mock(Section.class);
        RegistrationRecord record = mock(RegistrationRecord.class);
        
        when(studentRepo.findById(1)).thenReturn(Optional.of(student));
        when(sectionRepo.findById(10)).thenReturn(Optional.of(section));
        when(recordRepo.findByStudentIdAndSectionId(1, 10)).thenReturn(Optional.of(record));

        service.dropSection(1, 10, timestamp);

        verify(recordRepo).delete(record);

    }
}
