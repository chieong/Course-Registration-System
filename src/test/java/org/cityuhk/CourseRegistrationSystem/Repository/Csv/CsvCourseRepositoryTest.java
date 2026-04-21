package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CsvCourseRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvCourseRepository repo;
    private CsvFileStore store;
    private CsvIdGenerator idGen;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);
        repo = new CsvCourseRepository(store, idGen);
    }

    private Course buildCourse(String code, String title) {
        return new Course(code, title, 3, "desc", "2026A", new java.util.HashSet<>(), new java.util.HashSet<>(), null);
    }

    @Test
    void save_NewCourse_AssignsCourseId() {
        Course saved = repo.save(buildCourse("CS101", "Intro CS"));
        assertNotNull(saved.getCourseId());
        assertTrue(saved.getCourseId() > 0);
    }

    @Test
    void save_NewCourse_CanBeFoundByCode() {
        repo.save(buildCourse("CS101", "Intro CS"));
        Optional<Course> found = repo.findByCourseCode("CS101");
        assertTrue(found.isPresent());
        assertEquals("Intro CS", found.get().getTitle());
    }

    @Test
    void save_TwoCourses_GetDistinctIds() {
        Course a = repo.save(buildCourse("CS101", "Intro CS"));
        Course b = repo.save(buildCourse("CS102", "Data Structures"));
        assertNotEquals(a.getCourseId(), b.getCourseId());
    }

    @Test
    void save_ExistingCourse_UpdatesTitle() {
        Course saved = repo.save(buildCourse("CS101", "Intro CS"));
        saved.setTitle("Introduction to CS");
        repo.save(saved);

        Optional<Course> found = repo.findByCourseCode("CS101");
        assertTrue(found.isPresent());
        assertEquals("Introduction to CS", found.get().getTitle());
    }

    @Test
    void findByCourseCode_Unknown_ReturnsEmpty() {
        assertTrue(repo.findByCourseCode("UNKNOWN").isEmpty());
    }

    @Test
    void existsByCourseCode_Present_ReturnsTrue() {
        repo.save(buildCourse("CS101", "Intro CS"));
        assertTrue(repo.existsByCourseCode("CS101"));
    }

    @Test
    void existsByCourseCode_Absent_ReturnsFalse() {
        assertFalse(repo.existsByCourseCode("NONE"));
    }

    @Test
    void getCourseByCourseCode_Present_ReturnsCourse() {
        repo.save(buildCourse("CS101", "Intro CS"));
        Course c = repo.getCourseByCourseCode("CS101");
        assertNotNull(c);
        assertEquals("CS101", c.getCourseCode());
    }

    @Test
    void getCourseByCourseCode_Absent_ReturnsNull() {
        assertNull(repo.getCourseByCourseCode("NONE"));
    }

    @Test
    void delete_RemovesCourse() {
        Course saved = repo.save(buildCourse("CS101", "Intro CS"));
        repo.delete(saved);
        assertTrue(repo.findByCourseCode("CS101").isEmpty());
    }

    @Test
    void findAll_ReturnsAllCourses() {
        repo.save(buildCourse("CS101", "Intro CS"));
        repo.save(buildCourse("CS102", "Data Structures"));
        List<Course> all = repo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void findAll_EmptyStore_ReturnsEmptyList() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void persistence_AcrossRepoInstances() {
        repo.save(buildCourse("CS101", "Intro CS"));

        CsvCourseRepository repo2 = new CsvCourseRepository(store, idGen);
        assertTrue(repo2.findByCourseCode("CS101").isPresent());
    }

    @Test
    void save_CourseWithPrerequisite_Persists() {
        Course prereq = repo.save(buildCourse("CS100", "PreCS"));
        Course main = buildCourse("CS101", "Intro CS");
        main.setPrerequisiteCourses(Set.of(prereq));
        Course saved = repo.save(main);

        // Reload and check
        CsvCourseRepository repo2 = new CsvCourseRepository(store, idGen);
        Optional<Course> found = repo2.findByCourseCode("CS101");
        assertTrue(found.isPresent());
        assertFalse(found.get().getPrerequisiteCourses().isEmpty());
    }
}
