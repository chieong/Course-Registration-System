package org.cityuhk.CourseRegistrationSystem;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

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

    // @Test
    // void addSectionCallsSaveWhenAllSuccessfulTest() {
    //     StudentRepository studentRepo = mock(StudentRepository.class);
    //     SectionRepository sectionRepo = mock(SectionRepository.class);
    //     RegistrationRecordRepository recordRepo = mock(RegistrationRecordRepository.class);
    //     RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

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

        // Force enrollment check to pass
        Section section = new Section() {
            @Override
            public boolean canEnroll(Student s, int enrolled) {
                return true;
            }
        };

        class stubstudentrepo implements StudentRepository {

            @Override
            public void deleteAllByIdInBatch(Iterable<Integer> ids) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'deleteAllByIdInBatch'");
            }

            @Override
            public void deleteAllInBatch() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
            }

            @Override
            public void deleteAllInBatch(Iterable<Student> entities) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
            }

            @Override
            public <S extends Student> List<S> findAll(Example<S> example) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findAll'");
            }

            @Override
            public <S extends Student> List<S> findAll(Example<S> example, Sort sort) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findAll'");
            }

            @Override
            public void flush() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'flush'");
            }

            @Override
            public Student getById(Integer arg0) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getById'");
            }

            @Override
            public Student getOne(Integer arg0) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getOne'");
            }

            @Override
            public Student getReferenceById(Integer id) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getReferenceById'");
            }

            @Override
            public <S extends Student> List<S> saveAllAndFlush(Iterable<S> entities) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'saveAllAndFlush'");
            }

            @Override
            public <S extends Student> S saveAndFlush(S entity) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'saveAndFlush'");
            }

            @Override
            public List<Student> findAll() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findAll'");
            }

            @Override
            public List<Student> findAllById(Iterable<Integer> ids) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findAllById'");
            }

            @Override
            public <S extends Student> List<S> saveAll(Iterable<S> entities) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'saveAll'");
            }

            @Override
            public long count() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'count'");
            }

            @Override
            public void delete(Student entity) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'delete'");
            }

            @Override
            public void deleteAll() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
            }

            @Override
            public void deleteAll(Iterable<? extends Student> entities) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
            }

            @Override
            public void deleteAllById(Iterable<? extends Integer> ids) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'deleteAllById'");
            }

            @Override
            public void deleteById(Integer id) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
            }

            @Override
            public boolean existsById(Integer id) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'existsById'");
            }

            @Override
            public Optional<Student> findById(Integer id) {
                // TODO Auto-generated method stub
                return Optional.of(student);
                //throw new UnsupportedOperationException("Unimplemented method 'findById'");
            }

            @Override
            public <S extends Student> S save(S entity) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'save'");
            }

            @Override
            public List<Student> findAll(Sort sort) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findAll'");
            }

            @Override
            public Page<Student> findAll(Pageable pageable) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findAll'");
            }

            @Override
            public <S extends Student> long count(Example<S> example) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'count'");
            }

            @Override
            public <S extends Student> boolean exists(Example<S> example) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'exists'");
            }

            @Override
            public <S extends Student> Page<S> findAll(Example<S> example, Pageable pageable) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findAll'");
            }

            @Override
            public <S extends Student, R> R findBy(Example<S> arg0, Function<FetchableFluentQuery<S>, R> arg1) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findBy'");
            }

            @Override
            public <S extends Student> Optional<S> findOne(Example<S> example) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findOne'");
            }

            @Override
            public Optional<Student> findByUserEID(String userEID) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'findByUserEID'");
            }
            





        }



        LocalDateTime timestamp = LocalDateTime.now();
        Semester semester = new Semester(timestamp.minusDays(1), timestamp.plusDays(120));

        AtomicBoolean saveCalled = new AtomicBoolean(false);
        AtomicReference<RegistrationRecord> savedRecord = new AtomicReference<>();

        // StudentRepository studentRepo = (StudentRepository) Proxy.newProxyInstance(
        //         StudentRepository.class.getClassLoader(),
        //         new Class<?>[] { StudentRepository.class },
        //         (proxy, method, args) -> {
        //             if (method.getName().equals("findById")) return Optional.of(student);
        //             if (method.getName().equals("toString")) return "StudentRepoStub";
        //             if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
        //             if (method.getName().equals("equals")) return proxy == args[0];
        //             throw new UnsupportedOperationException("Not stubbed: " + method.getName());
        //         });

        SectionRepository sectionRepo = (SectionRepository) Proxy.newProxyInstance(
                SectionRepository.class.getClassLoader(),
                new Class<?>[] { SectionRepository.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("findById")) return Optional.of(section);
                    if (method.getName().equals("toString")) return "SectionRepoStub";
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    if (method.getName().equals("equals")) return proxy == args[0];
                    throw new UnsupportedOperationException("Not stubbed: " + method.getName());
                });

        RegistrationRecordRepository recordRepo = (RegistrationRecordRepository) Proxy.newProxyInstance(
                RegistrationRecordRepository.class.getClassLoader(),
                new Class<?>[] { RegistrationRecordRepository.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("exists")) return false;
                    if (method.getName().equals("countEnrolled")) return 5;
                    if (method.getName().equals("save")) {
                        saveCalled.set(true);
                        savedRecord.set((RegistrationRecord) args[0]);
                        return args[0];
                    }
                    if (method.getName().equals("toString")) return "RecordRepoStub";
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    if (method.getName().equals("equals")) return proxy == args[0];
                    throw new UnsupportedOperationException("Not stubbed: " + method.getName());
                });

        StudentRepository studentRepo = new stubstudentrepo();
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        service.addSection(1, 10, timestamp, semester);

        assertTrue(saveCalled.get(), "Expected save(...) to be called");
        assertNotNull(savedRecord.get(), "Expected saved RegistrationRecord");
    }
}
