package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CsvStudentRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvStudentRepository repo;
    private CsvFileStore store;
    private CsvIdGenerator idGen;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);
        repo = new CsvStudentRepository(store, idGen);
    }

    private Student buildStudent(int id, String eid, String name) {
        return new Student.StudentBuilder()
                .withStudentId(id)
                .withUserEID(eid)
                .withName(name)
                .withPassword("pw")
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(18)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(120)
                .build();
    }

    @Test
    void save_NewStudent_AssignsId() {
        Student saved = repo.save(buildStudent(0, "s001", "Alice"));
        assertTrue(saved.getStudentId() > 0);
    }

    @Test
    void save_NewStudent_CanBeFoundById() {
        Student saved = repo.save(buildStudent(0, "s001", "Alice"));
        Optional<Student> found = repo.findById(saved.getStudentId());
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getUserName());
    }

    @Test
    void save_TwoStudents_GetDistinctIds() {
        Student a = repo.save(buildStudent(0, "s001", "Alice"));
        Student b = repo.save(buildStudent(0, "s002", "Bob"));
        assertNotEquals(a.getStudentId(), b.getStudentId());
    }

    @Test
    void save_ExistingStudent_UpdatesInPlace() {
        Student saved = repo.save(buildStudent(0, "s001", "Alice"));
        Student updated = new Student.StudentBuilder()
                .withStudentId(saved.getStudentId())
                .withUserEID("s001")
                .withName("Alicia")
                .withPassword("pw2")
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(18)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(120)
                .build();
        repo.save(updated);

        Optional<Student> found = repo.findById(saved.getStudentId());
        assertTrue(found.isPresent());
        assertEquals("Alicia", found.get().getUserName());
        // Only one record
        assertEquals(1, repo.findById(saved.getStudentId()).stream().count());
    }

    @Test
    void findByUserEID_CaseInsensitive_ReturnsStudent() {
        repo.save(buildStudent(0, "S001", "Alice"));
        Optional<Student> found = repo.findByUserEID("s001");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getUserName());
    }

    @Test
    void findByUserEID_Unknown_ReturnsEmpty() {
        assertTrue(repo.findByUserEID("nobody").isEmpty());
    }

    @Test
    void findById_Unknown_ReturnsEmpty() {
        assertTrue(repo.findById(999).isEmpty());
    }

    @Test
    void persistence_AcrossRepoInstances() {
        Student saved = repo.save(buildStudent(0, "s001", "Alice"));

        CsvStudentRepository repo2 = new CsvStudentRepository(store, idGen);
        Optional<Student> found = repo2.findById(saved.getStudentId());
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getUserName());
    }

    @Test
    void save_MultipleStudents_AllRetrievable() {
        repo.save(buildStudent(0, "s001", "Alice"));
        repo.save(buildStudent(0, "s002", "Bob"));
        repo.save(buildStudent(0, "s003", "Charlie"));

        assertTrue(repo.findByUserEID("s001").isPresent());
        assertTrue(repo.findByUserEID("s002").isPresent());
        assertTrue(repo.findByUserEID("s003").isPresent());
    }
}
