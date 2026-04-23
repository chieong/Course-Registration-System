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
    private CsvStudentRepository studentRepo;
    private CsvCourseRepository courseRepo;
    private CsvSectionRepository sectionRepo;
    private Student student;
    private Section section;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);

        studentRepo = new CsvStudentRepository(store, idGen);
        courseRepo = new CsvCourseRepository(store, idGen);
        sectionRepo = new CsvSectionRepository(store, idGen, courseRepo);
        repo = new CsvRegistrationRecordRepository(store, idGen, studentRepo, sectionRepo);

        // Save a student and section to reference
        student = studentRepo.save(new Student.StudentBuilder()
                .withStudentId(0).withUserEID("s001").withName("Alice")
                .withPassword("pw").withMinSemesterCredit(0).withMaxSemesterCredit(18)
                .withMajor("CS").withCohort(2024).withDepartment("CS").withMaxDegreeCredit(120)
                .build());

        Course course = courseRepo.save(new Course("CS101", "Intro CS", 3, null,
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

    private Student buildStudent(String userEid, String name) {
        return studentRepo.save(new Student.StudentBuilder()
                .withStudentId(0).withUserEID(userEid).withName(name)
                .withPassword("pw").withMinSemesterCredit(0).withMaxSemesterCredit(18)
                .withMajor("CS").withCohort(2024).withDepartment("CS").withMaxDegreeCredit(120)
                .build());
    }

    private Section buildSavedSection(String venue) {
        Section savedSection = new Section();
        savedSection.setSectionId(0);
        savedSection.setCourse(section.getCourse());
        savedSection.setEnrollCapacity(30);
        savedSection.setWaitlistCapacity(5);
        savedSection.setVenue(venue);
        savedSection.setType(Section.Type.LECTURE);
        return sectionRepo.save(savedSection);
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
        repo.save(new RegistrationRecord(student, buildSavedSection("Room B"), ts2));
        repo.save(new RegistrationRecord(student, buildSavedSection("Room C"), ts1));

        List<RegistrationRecord> found = repo.findByStudentId(student.getStudentId());
        assertEquals(2, found.size());
        assertEquals(ts1, found.get(0).getTimestamp());
        assertEquals(ts2, found.get(1).getTimestamp());
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

    @Test
    void findAllRecords_IgnoresMalformedRowsUnknownLinksAndAllowsBlankTimestamp() {
        store.writeRows(CsvRegistrationRecordRepository.FILE, CsvRegistrationRecordRepository.HEADER, List.of(
                new String[]{"1", String.valueOf(student.getStudentId()), String.valueOf(section.getSectionId())},
                new String[]{"bad", String.valueOf(student.getStudentId()), String.valueOf(section.getSectionId()), "2026-04-21T10:00:00"},
                new String[]{"2", "999999", String.valueOf(section.getSectionId()), "2026-04-21T10:00:00"},
                new String[]{"3", String.valueOf(student.getStudentId()), "999999", "2026-04-21T10:00:00"},
                new String[]{"4", String.valueOf(student.getStudentId()), String.valueOf(section.getSectionId()), ""}
        ));

        List<RegistrationRecord> all = repo.findAllRecords();

        assertEquals(1, all.size());
        assertEquals(4, all.get(0).getRecordId());
        assertNull(all.get(0).getTimestamp());
    }

    @Test
    void findByStudentId_SortsNullTimestampLast() {
        Section morningSection = buildSavedSection("Room B");
        Section unscheduledSection = buildSavedSection("Room C");
        LocalDateTime ts = LocalDateTime.of(2026, 4, 21, 9, 0);

        repo.save(new RegistrationRecord(student, unscheduledSection, null));
        repo.save(new RegistrationRecord(student, morningSection, ts));

        List<RegistrationRecord> found = repo.findByStudentId(student.getStudentId());

        assertEquals(2, found.size());
        assertEquals(ts, found.get(0).getTimestamp());
        assertNull(found.get(1).getTimestamp());
    }

    @Test
    void exists_AndFindByStudentIdAndSectionId_RequireBothIdentifiersToMatch() {
        Student otherStudent = buildStudent("s002", "Bob");
        Section otherSection = buildSavedSection("Room B");
        repo.save(buildRecord(LocalDateTime.of(2026, 4, 21, 10, 0)));

        assertFalse(repo.exists(otherStudent.getStudentId(), section.getSectionId()));
        assertFalse(repo.exists(student.getStudentId(), otherSection.getSectionId()));
        assertTrue(repo.findByStudentIdAndSectionId(otherStudent.getStudentId(), section.getSectionId()).isEmpty());
        assertTrue(repo.findByStudentIdAndSectionId(student.getStudentId(), otherSection.getSectionId()).isEmpty());
    }

    @Test
    void find_IncludesBoundaryTimestampsAndSkipsNullTimestamp() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 21, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 21, 11, 0);

        repo.save(new RegistrationRecord(student, buildSavedSection("Room B"), start));
        repo.save(new RegistrationRecord(student, buildSavedSection("Room C"), end));
        repo.save(new RegistrationRecord(student, buildSavedSection("Room D"), null));

        List<RegistrationRecord> found = repo.find(student.getStudentId(), start, end);

        assertEquals(2, found.size());
        assertEquals(List.of(start, end), found.stream().map(RegistrationRecord::getTimestamp).sorted().toList());
    }

    @Test
    void save_ExistingRecord_PreservesOtherRecords() {
        RegistrationRecord first = repo.save(new RegistrationRecord(
                student,
                buildSavedSection("Room B"),
                LocalDateTime.of(2026, 4, 21, 9, 0)));
        RegistrationRecord second = repo.save(new RegistrationRecord(
                student,
                buildSavedSection("Room C"),
                LocalDateTime.of(2026, 4, 21, 10, 0)));

        RegistrationRecord updated = new RegistrationRecord(student, first.getSection(), LocalDateTime.of(2026, 4, 21, 12, 0));
        updated.setRecordId(first.getRecordId());
        repo.save(updated);

        List<RegistrationRecord> all = repo.findByStudentId(student.getStudentId());
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(record -> record.getRecordId().equals(second.getRecordId())));
        assertEquals(LocalDateTime.of(2026, 4, 21, 12, 0),
                all.stream()
                        .filter(record -> record.getRecordId().equals(first.getRecordId()))
                        .findFirst()
                        .orElseThrow()
                        .getTimestamp());
    }
}
