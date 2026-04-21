package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CsvSectionRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvSectionRepository repo;
    private CsvCourseRepository courseRepo;
    private CsvFileStore store;
    private CsvIdGenerator idGen;
    private Course savedCourse;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);
        courseRepo = new CsvCourseRepository(store, idGen);
        repo = new CsvSectionRepository(store, idGen, courseRepo);

        Course c = new Course("CS101", "Intro CS", 3, "desc", "2026A",
                new java.util.HashSet<>(), new java.util.HashSet<>(), null);
        savedCourse = courseRepo.save(c);
    }

    private Section buildSection(int id, String venue) {
        Section s = new Section();
        s.setSectionId(id);
        s.setCourse(savedCourse);
        s.setEnrollCapacity(30);
        s.setWaitlistCapacity(10);
        s.setVenue(venue);
        s.setType(Section.Type.LECTURE);
        return s;
    }

    private Section buildSectionWithTime(int id, String venue, LocalDateTime start, LocalDateTime end) {
        Section s = buildSection(id, venue);
        setField(s, "startTime", start);
        setField(s, "endTime", end);
        return s;
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

    @Test
    void save_NewSection_AssignsId() {
        Section saved = repo.save(buildSection(0, "Room A"));
        assertTrue(saved.getSectionId() > 0);
    }

    @Test
    void save_NewSection_CanBeFoundById() {
        Section saved = repo.save(buildSection(0, "Room A"));
        Optional<Section> found = repo.findById(saved.getSectionId());
        assertTrue(found.isPresent());
        assertEquals("Room A", found.get().getVenue());
    }

    @Test
    void save_TwoSections_GetDistinctIds() {
        Section a = repo.save(buildSection(0, "Room A"));
        Section b = repo.save(buildSection(0, "Room B"));
        assertNotEquals(a.getSectionId(), b.getSectionId());
    }

    @Test
    void save_ExistingSection_UpdatesVenue() {
        Section saved = repo.save(buildSection(0, "Room A"));
        saved.setVenue("Room B");
        repo.save(saved);

        Optional<Section> found = repo.findById(saved.getSectionId());
        assertTrue(found.isPresent());
        assertEquals("Room B", found.get().getVenue());
    }

    @Test
    void findById_Unknown_ReturnsEmpty() {
        assertTrue(repo.findById(999).isEmpty());
    }

    @Test
    void deleteById_RemovesSection() {
        Section saved = repo.save(buildSection(0, "Room A"));
        repo.deleteById(saved.getSectionId());
        assertTrue(repo.findById(saved.getSectionId()).isEmpty());
    }

    @Test
    void deleteById_Unknown_DoesNotThrow() {
        assertDoesNotThrow(() -> repo.deleteById(999));
    }

    @Test
    void findAll_ReturnsAllSections() {
        repo.save(buildSection(0, "Room A"));
        repo.save(buildSection(0, "Room B"));
        List<Section> all = repo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void findAll_EmptyStore_ReturnsEmptyList() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void persistence_AcrossRepoInstances() {
        Section saved = repo.save(buildSection(0, "Room A"));

        CsvSectionRepository repo2 = new CsvSectionRepository(store, idGen, courseRepo);
        Optional<Section> found = repo2.findById(saved.getSectionId());
        assertTrue(found.isPresent());
        assertEquals("Room A", found.get().getVenue());
    }

    @Test
    void save_SectionWithTimes_PersistsTimes() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 21, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 21, 11, 0);
        Section saved = repo.save(buildSectionWithTime(0, "Hall C", start, end));

        Optional<Section> found = repo.findById(saved.getSectionId());
        assertTrue(found.isPresent());
        assertEquals(start, found.get().getStartTime());
        assertEquals(end, found.get().getEndTime());
    }

    @Test
    void save_SectionType_Persists() {
        Section saved = repo.save(buildSection(0, "Lab 1"));
        Optional<Section> found = repo.findById(saved.getSectionId());
        assertTrue(found.isPresent());
        assertEquals(Section.Type.LECTURE, found.get().getType());
    }
}
