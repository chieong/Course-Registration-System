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
    void loadAll_IgnoresMalformedRows_AndLoadsBlankTextFields() {
        store.writeRows(CsvStudentRepository.FILE, CsvStudentRepository.HEADER, java.util.List.of(
                new String[]{"1", "", "", "", "0", "18", "", "2024", "", "120"},
                new String[]{"2", "s002", "Bob", "pw", "0", "18", "CS", "2024", "CS"},
                new String[]{"bad", "s003", "Charlie", "pw", "0", "18", "CS", "2024", "CS", "120"}
        ));

        java.util.List<Student> all = repo.loadAll();

        assertEquals(1, all.size());
        assertEquals(1, all.get(0).getStudentId());
        assertEquals("", all.get(0).getUserEID());
        assertEquals("", all.get(0).getUserName());
        assertEquals("", all.get(0).getDepartment());
    }

    @Test
    void findByUserEID_BlankPersistedUserEid_DoesNotMatchOtherValues() {
        store.writeRows(CsvStudentRepository.FILE, CsvStudentRepository.HEADER, java.util.List.of(
                new String[]{"1", "", "Alice", "pw", "0", "18", "CS", "2024", "CS", "120"},
                new String[]{"2", "s002", "Bob", "pw", "0", "18", "CS", "2024", "CS", "120"}
        ));

        assertTrue(repo.findByUserEID("s001").isEmpty());
        assertTrue(repo.findByUserEID("s002").isPresent());
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

    @Test
    void save_NullTextFields_ArePersistedAsEmptyStrings() {
        Student saved = repo.save(new Student.StudentBuilder()
                .withStudentId(0)
                .withUserEID(null)
                .withName(null)
                .withPassword(null)
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(18)
                .withMajor(null)
                .withCohort(2024)
                .withDepartment(null)
                .withMaxDegreeCredit(120)
                .build());

        Student found = repo.findById(saved.getStudentId()).orElseThrow();
        assertEquals("", found.getUserEID());
        assertEquals("", found.getUserName());
        assertEquals("", found.getPassword());
        assertEquals("", found.getMajor());
        assertEquals("", found.getDepartment());
    }

    @Test
    void save_ExistingStudent_PreservesOtherStudents() {
        Student first = repo.save(buildStudent(0, "s001", "Alice"));
        Student second = repo.save(buildStudent(0, "s002", "Bob"));

        Student updated = new Student.StudentBuilder()
                .withStudentId(first.getStudentId())
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

        assertEquals("Alicia", repo.findById(first.getStudentId()).orElseThrow().getUserName());
        assertEquals("Bob", repo.findById(second.getStudentId()).orElseThrow().getUserName());
        assertEquals(2, repo.loadAll().size());
    }
}
