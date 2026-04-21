package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CsvRegistrationPlanRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvRegistrationPlanRepository repo;
    private CsvFileStore store;
    private CsvIdGenerator idGen;
    private Student student;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);
        CsvStudentRepository studentRepo = new CsvStudentRepository(store, idGen);
        repo = new CsvRegistrationPlanRepository(store, idGen, studentRepo);

        student = studentRepo.save(new Student.StudentBuilder()
                .withStudentId(0).withUserEID("s001").withName("Alice")
                .withPassword("pw").withMinSemesterCredit(0).withMaxSemesterCredit(18)
                .withMajor("CS").withCohort(2024).withDepartment("CS").withMaxDegreeCredit(120)
                .build());
    }

    private RegistrationPlan buildPlan(int priority) {
        return new RegistrationPlan(student, "2026A", priority);
    }

    @Test
    void save_NewPlan_AssignsPlanId() {
        RegistrationPlan saved = repo.save(buildPlan(1));
        assertNotNull(saved.getPlanId());
        assertTrue(saved.getPlanId() > 0);
    }

    @Test
    void save_NewPlan_CanBeFoundById() {
        RegistrationPlan saved = repo.save(buildPlan(1));
        Optional<RegistrationPlan> found = repo.findById(saved.getPlanId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getPriority());
    }

    @Test
    void save_TwoPlans_GetDistinctIds() {
        RegistrationPlan a = repo.save(buildPlan(1));
        RegistrationPlan b = repo.save(buildPlan(2));
        assertNotEquals(a.getPlanId(), b.getPlanId());
    }

    @Test
    void save_ExistingPlan_UpdatesPriority() {
        RegistrationPlan saved = repo.save(buildPlan(1));
        saved.setPriority(5);
        repo.save(saved);

        Optional<RegistrationPlan> found = repo.findById(saved.getPlanId());
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getPriority());
    }

    @Test
    void findById_Unknown_ReturnsEmpty() {
        assertTrue(repo.findById(999).isEmpty());
    }

    @Test
    void findByStudentId_ReturnsPlansForStudent() {
        repo.save(buildPlan(1));
        repo.save(buildPlan(2));
        List<RegistrationPlan> found = repo.findByStudentId(student.getStudentId());
        assertEquals(2, found.size());
    }

    @Test
    void findByStudentId_UnknownStudent_ReturnsEmpty() {
        assertTrue(repo.findByStudentId(9999).isEmpty());
    }

    @Test
    void findByStudentIdOrderByPriorityAsc_ReturnsSorted() {
        repo.save(buildPlan(3));
        repo.save(buildPlan(1));
        repo.save(buildPlan(2));

        List<RegistrationPlan> sorted = repo.findByStudentIdOrderByPriorityAsc(student.getStudentId());
        assertEquals(3, sorted.size());
        assertEquals(1, sorted.get(0).getPriority());
        assertEquals(2, sorted.get(1).getPriority());
        assertEquals(3, sorted.get(2).getPriority());
    }

    @Test
    void countByStudentId_ReturnsCorrectCount() {
        repo.save(buildPlan(1));
        repo.save(buildPlan(2));
        assertEquals(2, repo.countByStudentId(student.getStudentId()));
    }

    @Test
    void countByStudentId_NoPlans_ReturnsZero() {
        assertEquals(0, repo.countByStudentId(9999));
    }

    @Test
    void deleteById_RemovesPlan() {
        RegistrationPlan saved = repo.save(buildPlan(1));
        repo.deleteById(saved.getPlanId());
        assertTrue(repo.findById(saved.getPlanId()).isEmpty());
    }

    @Test
    void deleteById_Unknown_DoesNotThrow() {
        assertDoesNotThrow(() -> repo.deleteById(999));
    }

    @Test
    void findAll_ReturnsAllPlans() {
        repo.save(buildPlan(1));
        repo.save(buildPlan(2));
        assertEquals(2, repo.findAll().size());
    }

    @Test
    void findAll_EmptyStore_ReturnsEmptyList() {
        assertTrue(repo.findAll().isEmpty());
    }

     @Test
     void save_ApplyStatus_Persists() {
         RegistrationPlan saved = repo.save(buildPlan(1));
         saved.setApplyStatus(RegistrationPlan.ApplyStatus.APPLIED);
         saved.setApplySummary("All sections added");
         repo.save(saved);

         Optional<RegistrationPlan> found = repo.findById(saved.getPlanId());
         assertTrue(found.isPresent());
         assertEquals(RegistrationPlan.ApplyStatus.APPLIED, found.get().getApplyStatus());
         assertEquals("All sections added", found.get().getApplySummary());
     }

    @Test
    void persistence_AcrossRepoInstances() {
        RegistrationPlan saved = repo.save(buildPlan(1));

        CsvStudentRepository studentRepo2 = new CsvStudentRepository(store, idGen);
        CsvRegistrationPlanRepository repo2 = new CsvRegistrationPlanRepository(store, idGen, studentRepo2);
        assertTrue(repo2.findById(saved.getPlanId()).isPresent());
    }
}
