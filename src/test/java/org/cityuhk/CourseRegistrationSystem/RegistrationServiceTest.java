package org.cityuhk.CourseRegistrationSystem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Service.RegistrationService;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;


public class RegistrationServiceTest {

    // ============= Helper Method to Create Stub Repositories =============

    private StudentRepository createStubStudentRepo(Student student) {
        return new StudentRepository() {
            @Override
            public Optional<Student> findById(Integer id) {
                return Optional.ofNullable(student);
            }

            @Override
            public void deleteById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> S save(S entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<Student> findAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<Student> findAll(Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Page<Student> findAll(Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<Student> findAllById(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public long count() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void delete(Student entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll(Iterable<? extends Student> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllById(Iterable<? extends Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch(Iterable<Student> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllByIdInBatch(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void flush() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Student getById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Student getOne(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Student getReferenceById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> List<S> saveAll(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> List<S> saveAllAndFlush(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> S saveAndFlush(S entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public boolean existsById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> long count(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> boolean exists(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> List<S> findAll(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> List<S> findAll(Example<S> example, Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> Page<S> findAll(Example<S> example, Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student> Optional<S> findOne(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Student, R> R findBy(Example<S> example,
                    Function<FetchableFluentQuery<S>, R> queryFunction) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Optional<Student> findByUserEID(String userEID) {
                throw new UnsupportedOperationException("Not stubbed");
            }
        };
    }

    private SectionRepository createStubSectionRepo(Section section) {
        return new SectionRepository() {
            @Override
            public Optional<Section> findById(Integer id) {
                return Optional.ofNullable(section);
            }

            @Override
            public void deleteById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> S save(S entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<Section> findAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<Section> findAll(Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Page<Section> findAll(Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<Section> findAllById(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public long count() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void delete(Section entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll(Iterable<? extends Section> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllById(Iterable<? extends Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch(Iterable<Section> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllByIdInBatch(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void flush() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Section getById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Section getOne(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Section getReferenceById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> List<S> saveAll(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> List<S> saveAllAndFlush(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> S saveAndFlush(S entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public boolean existsById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> long count(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> boolean exists(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> List<S> findAll(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> List<S> findAll(Example<S> example, Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> Page<S> findAll(Example<S> example, Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section> Optional<S> findOne(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends Section, R> R findBy(Example<S> example,
                    Function<FetchableFluentQuery<S>, R> queryFunction) {
                throw new UnsupportedOperationException("Not stubbed");
            }
        };
    }

    private RegistrationRecordRepository createStubRecordRepo(boolean exists, int countEnrolled, 
            boolean allowSave, boolean allowDelete) {
        return new RegistrationRecordRepository() {
            @Override
            public boolean exists(Integer studentId, Integer sectionId) {
                return exists;
            }

            @Override
            public int countEnrolled(Integer sectionId) {
                return countEnrolled;
            }

            @Override
            public <S extends RegistrationRecord> S save(S entity) {
                if (allowSave) return entity;
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void delete(RegistrationRecord entity) {
                if (!allowDelete) throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAll(Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Page<RegistrationRecord> findAll(Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAllById(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public long count() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll(Iterable<? extends RegistrationRecord> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllById(Iterable<? extends Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch(Iterable<RegistrationRecord> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllByIdInBatch(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void flush() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getOne(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getReferenceById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> saveAll(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> saveAllAndFlush(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> S saveAndFlush(S entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Optional<RegistrationRecord> findById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public boolean existsById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> long count(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> boolean exists(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> findAll(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> findAll(Example<S> example, Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> Page<S> findAll(Example<S> example, Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> Optional<S> findOne(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord, R> R findBy(Example<S> example,
                    Function<FetchableFluentQuery<S>, R> queryFunction) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> find(Integer studentId, LocalDateTime start, LocalDateTime end) {
                throw new UnsupportedOperationException("Not stubbed");
            }
        };
    }

    // ============= addSection Tests =============

    @Test
    void addSectionThrowsWhenStudentNotFoundTest() {
        StudentRepository studentRepo = createStubStudentRepo(null);
        SectionRepository sectionRepo = createStubSectionRepo(null);
        RegistrationRecordRepository recordRepo = createStubRecordRepo(false, 0, false, false);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Student not found"));
    }

    @Test
    void addSectionThrowsWhenSectionNotFoundTest() {
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

        StudentRepository studentRepo = createStubStudentRepo(student);
        SectionRepository sectionRepo = createStubSectionRepo(null);
        RegistrationRecordRepository recordRepo = createStubRecordRepo(false, 0, false, false);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Section not found"));
    }

    @Test
    void addSectionThrowsWhenAlreadyEnrolledTest() {
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

        StudentRepository studentRepo = createStubStudentRepo(student);
        SectionRepository sectionRepo = createStubSectionRepo(section);
        RegistrationRecordRepository recordRepo = createStubRecordRepo(true, 5, false, false);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Already enrolled"));
    }

    @Test
    void addSectionCallsSaveWhenAllSuccessfulTest() {
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

        Section section = new Section() {
            @Override
            public boolean canEnroll(Student s, int enrolled) {
                return true;
            }
        };

        LocalDateTime timestamp = LocalDateTime.now();
        RegistrationRecord[] savedRecords = new RegistrationRecord[1];

        StudentRepository studentRepo = createStubStudentRepo(student);
        SectionRepository sectionRepo = createStubSectionRepo(section);
        RegistrationRecordRepository recordRepo = new RegistrationRecordRepository() {
            @Override
            public boolean exists(Integer studentId, Integer sectionId) {
                return false;
            }

            @Override
            public int countEnrolled(Integer sectionId) {
                return 5;
            }

            @Override
            public <S extends RegistrationRecord> S save(S entity) {
                savedRecords[0] = entity;
                return entity;
            }

            @Override
            public void delete(RegistrationRecord entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAll(Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Page<RegistrationRecord> findAll(Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAllById(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public long count() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll(Iterable<? extends RegistrationRecord> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllById(Iterable<? extends Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch(Iterable<RegistrationRecord> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllByIdInBatch(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void flush() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getOne(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getReferenceById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> saveAll(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> saveAllAndFlush(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> S saveAndFlush(S entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Optional<RegistrationRecord> findById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public boolean existsById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> long count(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> boolean exists(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> findAll(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> findAll(Example<S> example, Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> Page<S> findAll(Example<S> example, Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> Optional<S> findOne(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord, R> R findBy(Example<S> example,
                    Function<FetchableFluentQuery<S>, R> queryFunction) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> find(Integer studentId, LocalDateTime start, LocalDateTime end) {
                throw new UnsupportedOperationException("Not stubbed");
            }
        };

        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);
        service.addSection(1, 10, timestamp);

        assertNotNull(savedRecords[0], "Expected save(...) to be called");
    }

    // ============= dropSection Tests =============

    @Test
    void dropSectionThrowsWhenStudentNotFoundTest() {
        StudentRepository studentRepo = createStubStudentRepo(null);
        SectionRepository sectionRepo = createStubSectionRepo(null);
        RegistrationRecordRepository recordRepo = createStubRecordRepo(false, 0, false, false);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.dropSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Student not found"));
    }

    @Test
    void dropSectionThrowsWhenSectionNotFoundTest() {
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

        StudentRepository studentRepo = createStubStudentRepo(student);
        SectionRepository sectionRepo = createStubSectionRepo(null);
        RegistrationRecordRepository recordRepo = createStubRecordRepo(false, 0, false, false);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.dropSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Section not found"));
    }

    @Test
    void dropSectionThrowsWhenNotEnrolledTest() {
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

        StudentRepository studentRepo = createStubStudentRepo(student);
        SectionRepository sectionRepo = createStubSectionRepo(section);
        RegistrationRecordRepository recordRepo = createStubRecordRepo(false, 0, false, false);
        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.dropSection(1, 10, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Not enrolled"));
    }

    @Test
    void dropSectionCallsDeleteWhenSuccessfulTest() {
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
        RegistrationRecord[] deletedRecords = new RegistrationRecord[1];

        StudentRepository studentRepo = createStubStudentRepo(student);
        SectionRepository sectionRepo = createStubSectionRepo(section);
        RegistrationRecordRepository recordRepo = new RegistrationRecordRepository() {
            @Override
            public boolean exists(Integer studentId, Integer sectionId) {
                return true;
            }

            @Override
            public int countEnrolled(Integer sectionId) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> S save(S entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void delete(RegistrationRecord entity) {
                deletedRecords[0] = entity;
            }

            @Override
            public void deleteById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAll(Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Page<RegistrationRecord> findAll(Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> findAllById(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public long count() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAll(Iterable<? extends RegistrationRecord> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllById(Iterable<? extends Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllInBatch(Iterable<RegistrationRecord> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void deleteAllByIdInBatch(Iterable<Integer> ids) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public void flush() {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getOne(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public RegistrationRecord getReferenceById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> saveAll(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> saveAllAndFlush(Iterable<S> entities) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> S saveAndFlush(S entity) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public Optional<RegistrationRecord> findById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public boolean existsById(Integer id) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> long count(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> boolean exists(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> findAll(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> List<S> findAll(Example<S> example, Sort sort) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> Page<S> findAll(Example<S> example, Pageable pageable) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord> Optional<S> findOne(Example<S> example) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public <S extends RegistrationRecord, R> R findBy(Example<S> example,
                    Function<FetchableFluentQuery<S>, R> queryFunction) {
                throw new UnsupportedOperationException("Not stubbed");
            }

            @Override
            public List<RegistrationRecord> find(Integer studentId, LocalDateTime start, LocalDateTime end) {
                throw new UnsupportedOperationException("Not stubbed");
            }
        };

        RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);
        service.dropSection(1, 10, timestamp);

        assertNotNull(deletedRecords[0], "Expected delete(...) to be called");
    }
}
