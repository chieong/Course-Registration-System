package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CsvAdminRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvAdminRepository repo;

    @BeforeEach
    void setUp() {
        CsvFileStore store = new CsvFileStore(tempDir.toString());
        CsvIdGenerator idGen = new CsvIdGenerator(store);
        repo = new CsvAdminRepository(store, idGen);
    }

    private Admin buildAdmin(int staffId, String eid, String name, String password) {
        return new Admin.AdminBuilder()
                .withStaffId(staffId)
                .withUserEID(eid)
                .withName(name)
                .withPassword(password)
                .build();
    }

    // ── save (new entity) ─────────────────────────────────────────────────────

    @Test
    void save_NewAdmin_AssignsId() {
        Admin admin = buildAdmin(0, "e001", "Alice", "pw");
        Admin saved = repo.save(admin);

        assertTrue(saved.getStaffId() > 0, "Expected a generated staffId");
    }

    @Test
    void save_NewAdmin_CanBeRetrievedByEid() {
        repo.save(buildAdmin(0, "e001", "Alice", "pw"));

        Optional<Admin> found = repo.findByUserEID("e001");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getUserName());
    }

    @Test
    void save_TwoNewAdmins_GetDistinctIds() {
        Admin a1 = repo.save(buildAdmin(0, "e001", "Alice", "pw1"));
        Admin a2 = repo.save(buildAdmin(0, "e002", "Bob", "pw2"));

        assertNotEquals(a1.getStaffId(), a2.getStaffId());
    }

    // ── save (update existing) ────────────────────────────────────────────────

    @Test
    void save_ExistingAdmin_UpdatesRecord() {
        Admin saved = repo.save(buildAdmin(0, "e001", "Alice", "pw"));
        int id = saved.getStaffId();

        Admin updated = buildAdmin(id, "e001", "Alice Updated", "newpw");
        repo.save(updated);

        Admin found = repo.findById(id).orElseThrow();
        assertEquals("Alice Updated", found.getUserName());
        assertEquals(1, repo.findAll().size());
    }

    // ── findByUserEID ─────────────────────────────────────────────────────────

    @Test
    void findByUserEID_CaseInsensitive() {
        repo.save(buildAdmin(0, "E001", "Alice", "pw"));

        assertTrue(repo.findByUserEID("e001").isPresent());
        assertTrue(repo.findByUserEID("E001").isPresent());
    }

    @Test
    void findByUserEID_UnknownEid_ReturnsEmpty() {
        assertTrue(repo.findByUserEID("ghost").isEmpty());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_ExistingId_ReturnsAdmin() {
        Admin saved = repo.save(buildAdmin(0, "e001", "Alice", "pw"));

        Optional<Admin> found = repo.findById(saved.getStaffId());
        assertTrue(found.isPresent());
    }

    @Test
    void findById_UnknownId_ReturnsEmpty() {
        assertTrue(repo.findById(999).isEmpty());
    }

    // ── existsById ────────────────────────────────────────────────────────────

    @Test
    void existsById_ReturnsTrueForSaved() {
        Admin saved = repo.save(buildAdmin(0, "e001", "Alice", "pw"));
        assertTrue(repo.existsById(saved.getStaffId()));
    }

    @Test
    void existsById_ReturnsFalseForUnknown() {
        assertFalse(repo.existsById(999));
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    void deleteById_RemovesAdmin() {
        Admin saved = repo.save(buildAdmin(0, "e001", "Alice", "pw"));
        repo.deleteById(saved.getStaffId());

        assertTrue(repo.findById(saved.getStaffId()).isEmpty());
    }

    @Test
    void deleteById_UnknownId_NoError() {
        assertDoesNotThrow(() -> repo.deleteById(999));
    }

    // ── findAll / count ───────────────────────────────────────────────────────

    @Test
    void findAll_ReturnsAllSavedAdmins() {
        repo.save(buildAdmin(0, "e001", "Alice", "pw"));
        repo.save(buildAdmin(0, "e002", "Bob", "pw"));

        List<Admin> all = repo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void count_ReflectsNumberOfRecords() {
        assertEquals(0, repo.count());
        repo.save(buildAdmin(0, "e001", "Alice", "pw"));
        assertEquals(1, repo.count());
        repo.save(buildAdmin(0, "e002", "Bob", "pw"));
        assertEquals(2, repo.count());
    }

    // ── persistence across instances ──────────────────────────────────────────

    @Test
    void savedData_PersistedAcrossRepoInstances() {
        CsvFileStore store = new CsvFileStore(tempDir.toString());
        CsvIdGenerator idGen = new CsvIdGenerator(store);
        CsvAdminRepository first = new CsvAdminRepository(store, idGen);
        first.save(buildAdmin(0, "e001", "Alice", "pw"));

        // New repo instance reading from same directory
        CsvAdminRepository second = new CsvAdminRepository(store, new CsvIdGenerator(store));
        assertEquals(1, second.findAll().size());
        assertTrue(second.findByUserEID("e001").isPresent());
    }
}
