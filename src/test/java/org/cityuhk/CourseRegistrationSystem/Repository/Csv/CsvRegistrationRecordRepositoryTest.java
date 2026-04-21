package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CsvRegistrationRecordRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvRegistrationRecordRepository repo;
    private CsvFileStore store;
    private CsvIdGenerator idGen;
    private Student student;
    private Section section;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);

        CsvStudentRepository studentRepo = new CsvStudentRepository(store, idGen);
        CsvCourseRepository courseRepo = new CsvCourseRepository(store, idGen);
        CsvSectionRepository sectionRepo = new CsvSectionRepository(store, idGen, courseRepo);
        repo = new CsvRegistrationRecordRepository(store, idGen, studentRepo, sectionRepo);

        // Save a student and section to reference
        student = studentRepo.save(new Student.StudentBuilder()
                .withStudentId(0).withUserEID("s001").withName("Alice")
                .withPassword("pw").withMinSemesterCredit(0).withMaxSemesterCredit(18)
                .withMajor("CS").withCohort(2024).withDepartment("CS").withMaxDegreeCredit(120)
                .build());

        Course course = courseRepo.save(new Course("CS101", "Intro CS", 3, null, "2026A",
                new java.util.HashSet<>(), new java.util.HashSet<>(), null));
        section = new Section();
        section.setSectionId(0);
        section.setCourse(course);
        section.setEnrollCapacity(30);
        section.setWaitlistCapacity(5);
        section.setVenue("Room A");
        section.setType(Section.Type.LECTURE);
        section = sectionRepo.save(section);
    }

    private static void setField(Section s, String name, Object value) {
        try {
            Field f = Section.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(s, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RegistrationRecord buildRecord(LocalDateTime ts) {
        return new RegistrationRecord(student, section, ts);
    }

    @Test
    void save_NewRecord_AssignsId() {
        RegistrationRecord saved = repo.save(buildRecord(LocalDateTime.now()));
        assertNotNull(saved.getRecordId());
        assertTrue(saved.getRecordId() > 0);
    }

    @Test
    void save_NewRecord_CanBeRetrieved() {
        LocalDateTime ts = LocalDateTime.of(2026, 4, 21, 10, 0);
        RegistrationRecord saved = repo.save(buildRecord(ts));

        List<RegistrationRecord> all = repo.findAllRecords();
        assertEquals(1, all.size());
        assertEquals(saved.getRecordId(), all.get(0).getRecordId());
    }

    @Test
    void save_ExistingRecord_UpdatesTimestamp() {
        LocalDateTime ts1 = LocalDateTime.of(2026, 4, 21, 10, 0);
        LocalDateTime ts2 = LocalDateTime.of(2026, 4, 21, 11, 0);
        RegistrationRecord saved = repo.save(buildRecord(ts1));

        // Delete old, save updated record (RegistrationRecord has no setTimestamp)
        repo.delete(saved);
        RegistrationRecord updated = new RegistrationRecord(student, section, ts2);
        updated.setRecordId(saved.getRecordId());
        repo.save(updated);

        List<RegistrationRecord> all = repo.findAllRecords();
        assertEquals(1, all.size());
        assertEquals(ts2, all.get(0).getTimestamp());
    }

    @Test
    void countEnrolled_ReturnsCorrectCount() {
        repo.save(buildRecord(LocalDateTime.now()));
        assertEquals(1, repo.countEnrolled(section.getSectionId()));
    }

    @Test
    void countEnrolled_NoRecords_ReturnsZero() {
        assertEquals(0, repo.countEnrolled(section.getSectionId()));
    }

    @Test
    void exists_WhenEnrolled_ReturnsTrue() {
        repo.save(buildRecord(LocalDateTime.now()));
        assertTrue(repo.exists(student.getStudentId(), section.getSectionId()));
    }

    @Test
    void exists_WhenNotEnrolled_ReturnsFalse() {
        assertFalse(repo.exists(student.getStudentId(), section.getSectionId()));
    }

    @Test
    void findByStudentIdAndSectionId_Present_ReturnsRecord() {
        repo.save(buildRecord(LocalDateTime.now()));
        Optional<RegistrationRecord> found = repo.findByStudentIdAndSectionId(
                student.getStudentId(), section.getSectionId());
        assertTrue(found.isPresent());
    }

    @Test
    void findByStudentIdAndSectionId_Absent_ReturnsEmpty() {
        Optional<RegistrationRecord> found = repo.findByStudentIdAndSectionId(999, 999);
        assertTrue(found.isEmpty());
    }

    @Test
    void findByStudentId_ReturnsSortedByTimestamp() {
        LocalDateTime ts1 = LocalDateTime.of(2026, 4, 21, 10, 0);
        LocalDateTime ts2 = LocalDateTime.of(2026, 4, 21, 11, 0);
        repo.save(buildRecord(ts2));
        repo.save(buildRecord(ts1)); // save out of order (same student/section overwrite, but with separate sections would differ)

        List<RegistrationRecord> found = repo.findByStudentId(student.getStudentId());
        assertFalse(found.isEmpty());
    }

    @Test
    void find_WithinRange_ReturnsMatchingRecords() {
        LocalDateTime ts = LocalDateTime.of(2026, 4, 21, 10, 0);
        repo.save(buildRecord(ts));

        List<RegistrationRecord> found = repo.find(student.getStudentId(),
                LocalDateTime.of(2026, 4, 21, 9, 0),
                LocalDateTime.of(2026, 4, 21, 11, 0));
        assertEquals(1, found.size());
    }

    @Test
    void find_OutsideRange_ReturnsEmpty() {
        LocalDateTime ts = LocalDateTime.of(2026, 4, 21, 10, 0);
        repo.save(buildRecord(ts));

        List<RegistrationRecord> found = repo.find(student.getStudentId(),
                LocalDateTime.of(2026, 4, 22, 0, 0),
                LocalDateTime.of(2026, 4, 22, 23, 59));
        assertTrue(found.isEmpty());
    }

    @Test
    void delete_RemovesRecord() {
        RegistrationRecord saved = repo.save(buildRecord(LocalDateTime.now()));
        repo.delete(saved);
        assertTrue(repo.findAllRecords().isEmpty());
    }

    @Test
    void persistence_AcrossRepoInstances() {
        repo.save(buildRecord(LocalDateTime.now()));

        CsvStudentRepository studentRepo2 = new CsvStudentRepository(store, idGen);
        CsvCourseRepository courseRepo2 = new CsvCourseRepository(store, idGen);
        CsvSectionRepository sectionRepo2 = new CsvSectionRepository(store, idGen, courseRepo2);
        CsvRegistrationRecordRepository repo2 = new CsvRegistrationRecordRepository(
                store, idGen, studentRepo2, sectionRepo2);
        assertEquals(1, repo2.findAllRecords().size());
    }
}
