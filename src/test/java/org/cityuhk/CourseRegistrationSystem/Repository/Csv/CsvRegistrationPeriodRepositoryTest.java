package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CsvRegistrationPeriodRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvRegistrationPeriodRepository repo;
    private CsvFileStore store;
    private CsvIdGenerator idGen;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);
        repo = new CsvRegistrationPeriodRepository(store, idGen);
    }

    private RegistrationPeriod buildPeriod(int cohort, LocalDateTime start, LocalDateTime end) {
        RegistrationPeriod rp = new RegistrationPeriod();
        rp.setCohort(cohort);
        rp.setStartDateTime(start);
        rp.setEndDateTime(end);
        rp.setTerm("2026A");
        return rp;
    }

    @Test
    void save_NewPeriod_AssignsId() {
        RegistrationPeriod period = buildPeriod(2024,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 23, 59));
        RegistrationPeriod saved = repo.save(period);
        assertTrue(saved.getPeriodId() > 0);
    }

    @Test
    void save_NewPeriod_CanBeFoundById() {
        RegistrationPeriod saved = repo.save(buildPeriod(2024,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 23, 59)));

        Optional<RegistrationPeriod> found = repo.findById(saved.getPeriodId());
        assertTrue(found.isPresent());
        assertEquals(2024, found.get().getCohort());
        assertEquals("2026A", found.get().getTerm());
    }

    @Test
    void save_TwoPeriods_GetDistinctIds() {
        RegistrationPeriod a = repo.save(buildPeriod(2024,
                LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 4, 15, 0, 0)));
        RegistrationPeriod b = repo.save(buildPeriod(2025,
                LocalDateTime.of(2026, 5, 1, 0, 0), LocalDateTime.of(2026, 5, 15, 0, 0)));
        assertNotEquals(a.getPeriodId(), b.getPeriodId());
    }

    @Test
    void save_ExistingPeriod_UpdatesTerm() {
        RegistrationPeriod saved = repo.save(buildPeriod(2024,
                LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 4, 30, 23, 59)));
        saved.setTerm("2026B");
        repo.save(saved);

        Optional<RegistrationPeriod> found = repo.findById(saved.getPeriodId());
        assertTrue(found.isPresent());
        assertEquals("2026B", found.get().getTerm());
    }

    @Test
    void findById_Unknown_ReturnsEmpty() {
        assertTrue(repo.findById(999).isEmpty());
    }

    @Test
    void findAll_ReturnsAllPeriods() {
        repo.save(buildPeriod(2024, LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 4, 15, 0, 0)));
        repo.save(buildPeriod(2025, LocalDateTime.of(2026, 5, 1, 0, 0), LocalDateTime.of(2026, 5, 15, 0, 0)));
        List<RegistrationPeriod> all = repo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void findAll_EmptyStore_ReturnsEmptyList() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void findActivePeriod_NowWithinRange_ReturnsPeriod() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59);
        repo.save(buildPeriod(2024, start, end));

        LocalDateTime now = LocalDateTime.of(2026, 4, 21, 12, 0);
        Optional<RegistrationPeriod> active = repo.findActivePeriod(2024, now);
        assertTrue(active.isPresent());
        assertEquals(2024, active.get().getCohort());
    }

    @Test
    void findActivePeriod_NowBeforeStart_ReturnsEmpty() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 31, 23, 59);
        repo.save(buildPeriod(2024, start, end));

        LocalDateTime now = LocalDateTime.of(2026, 4, 21, 12, 0);
        assertTrue(repo.findActivePeriod(2024, now).isEmpty());
    }

    @Test
    void findActivePeriod_NowAfterEnd_ReturnsEmpty() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 31, 23, 59);
        repo.save(buildPeriod(2024, start, end));

        LocalDateTime now = LocalDateTime.of(2026, 4, 21, 12, 0);
        assertTrue(repo.findActivePeriod(2024, now).isEmpty());
    }

    @Test
    void findActivePeriod_DifferentCohort_ReturnsEmpty() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59);
        repo.save(buildPeriod(2024, start, end));

        LocalDateTime now = LocalDateTime.of(2026, 4, 21, 12, 0);
        assertTrue(repo.findActivePeriod(9999, now).isEmpty());
    }

    @Test
    void save_PeriodWithNullDates_DoesNotThrow() {
        RegistrationPeriod period = new RegistrationPeriod();
        period.setCohort(2024);
        period.setTerm("2026A");
        assertDoesNotThrow(() -> repo.save(period));
    }

    @Test
    void persistence_AcrossRepoInstances() {
        RegistrationPeriod saved = repo.save(buildPeriod(2024,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 23, 59)));

        CsvRegistrationPeriodRepository repo2 = new CsvRegistrationPeriodRepository(store, idGen);
        Optional<RegistrationPeriod> found = repo2.findById(saved.getPeriodId());
        assertTrue(found.isPresent());
        assertEquals(2024, found.get().getCohort());
    }
}
