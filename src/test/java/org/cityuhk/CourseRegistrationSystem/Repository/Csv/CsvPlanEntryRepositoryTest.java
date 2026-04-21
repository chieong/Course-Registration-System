package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CsvPlanEntryRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvPlanEntryRepository repo;
    private CsvFileStore store;
    private CsvIdGenerator idGen;
    private RegistrationPlan plan;
    private Section section;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);

        CsvStudentRepository studentRepo = new CsvStudentRepository(store, idGen);
        CsvCourseRepository courseRepo = new CsvCourseRepository(store, idGen);
        CsvSectionRepository sectionRepo = new CsvSectionRepository(store, idGen, courseRepo);
        CsvRegistrationPlanRepository planRepo = new CsvRegistrationPlanRepository(store, idGen, studentRepo);
        repo = new CsvPlanEntryRepository(store, idGen, planRepo, sectionRepo);

        Student student = studentRepo.save(new Student.StudentBuilder()
                .withStudentId(0).withUserEID("s001").withName("Alice")
                .withPassword("pw").withMinSemesterCredit(0).withMaxSemesterCredit(18)
                .withMajor("CS").withCohort(2024).withDepartment("CS").withMaxDegreeCredit(120)
                .build());

        Course course = courseRepo.save(new Course("CS101", "Intro CS", 3, null, "2026A",
                new java.util.HashSet<>(), new java.util.HashSet<>(), null));

        Section s = new Section();
        s.setSectionId(0);
        s.setCourse(course);
        s.setEnrollCapacity(30);
        s.setWaitlistCapacity(5);
        s.setVenue("Room A");
        s.setType(Section.Type.LECTURE);
        section = sectionRepo.save(s);

        plan = planRepo.save(new RegistrationPlan(student, "2026A", 1));
    }

     private PlanEntry buildEntry() {
         return new PlanEntry(plan, section, PlanEntry.EntryType.SELECTED);
     }

    @Test
    void save_NewEntry_AssignsId() {
        PlanEntry saved = repo.save(buildEntry());
        assertNotNull(saved.getEntryId());
        assertTrue(saved.getEntryId() > 0);
    }

    @Test
    void save_NewEntry_CanBeFoundById() {
        PlanEntry saved = repo.save(buildEntry());
        Optional<PlanEntry> found = repo.findById(saved.getEntryId());
        assertTrue(found.isPresent());
        assertEquals("ADD", found.get().getEntryType());
    }

    @Test
    void save_TwoEntries_GetDistinctIds() {
        PlanEntry a = repo.save(buildEntry());
        PlanEntry b = repo.save(buildEntry());
        assertNotEquals(a.getEntryId(), b.getEntryId());
    }

     @Test
     void save_ExistingEntry_UpdatesStatus() {
         PlanEntry saved = repo.save(buildEntry());
         saved.setStatus(PlanEntry.EntryStatus.FAILED);
         repo.save(saved);

         Optional<PlanEntry> found = repo.findById(saved.getEntryId());
         assertTrue(found.isPresent());
         assertEquals(PlanEntry.EntryStatus.FAILED, found.get().getStatus());
     }

    @Test
    void findById_Unknown_ReturnsEmpty() {
        assertTrue(repo.findById(999).isEmpty());
    }

    @Test
    void findByPlanId_ReturnsEntriesForPlan() {
        repo.save(buildEntry());
        repo.save(buildEntry());
        List<PlanEntry> found = repo.findByPlanId(plan.getPlanId());
        assertEquals(2, found.size());
    }

    @Test
    void findByPlanId_UnknownPlan_ReturnsEmpty() {
        assertTrue(repo.findByPlanId(9999).isEmpty());
    }

    @Test
    void deleteById_RemovesEntry() {
        PlanEntry saved = repo.save(buildEntry());
        repo.deleteById(saved.getEntryId());
        assertTrue(repo.findById(saved.getEntryId()).isEmpty());
    }

    @Test
    void deleteById_Unknown_DoesNotThrow() {
        assertDoesNotThrow(() -> repo.deleteById(999));
    }

    @Test
    void deleteByPlanId_RemovesAllEntriesForPlan() {
        repo.save(buildEntry());
        repo.save(buildEntry());
        repo.deleteByPlanId(plan.getPlanId());
        assertTrue(repo.findByPlanId(plan.getPlanId()).isEmpty());
    }

    @Test
    void save_JoinWaitlistFlag_Persists() {
        PlanEntry entry = buildEntry();
        entry.setJoinWaitlistOnAddFailure(true);
        PlanEntry saved = repo.save(entry);

        Optional<PlanEntry> found = repo.findById(saved.getEntryId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isJoinWaitlistOnAddFailure());
    }

    @Test
    void persistence_AcrossRepoInstances() {
        PlanEntry saved = repo.save(buildEntry());

        CsvStudentRepository studentRepo2 = new CsvStudentRepository(store, idGen);
        CsvCourseRepository courseRepo2 = new CsvCourseRepository(store, idGen);
        CsvSectionRepository sectionRepo2 = new CsvSectionRepository(store, idGen, courseRepo2);
        CsvRegistrationPlanRepository planRepo2 = new CsvRegistrationPlanRepository(store, idGen, studentRepo2);
        CsvPlanEntryRepository repo2 = new CsvPlanEntryRepository(store, idGen, planRepo2, sectionRepo2);

        Optional<PlanEntry> found = repo2.findById(saved.getEntryId());
        assertTrue(found.isPresent());
        assertEquals("ADD", found.get().getEntryType());
    }
}
