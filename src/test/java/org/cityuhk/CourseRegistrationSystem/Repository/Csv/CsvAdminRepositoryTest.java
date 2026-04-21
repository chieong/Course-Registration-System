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
    private CsvFileStore store;
    private CsvIdGenerator idGen;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);
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

    @Test
    void findAll_IgnoresMalformedRows_AndAllowsBlankUserEid() {
        store.writeRows(CsvAdminRepository.FILE, CsvAdminRepository.HEADER, List.of(
                new String[]{"1", "", "Alice", "pw"},
                new String[]{"2", "e002", "Bob"},
                new String[]{"bad", "e003", "Charlie", "pw3"}
        ));

        List<Admin> all = repo.findAll();

        assertEquals(1, all.size());
        assertEquals(1, all.get(0).getStaffId());
        assertEquals("", all.get(0).getUserEID());
    }

    @Test
    void findByUserEID_IgnoresRowsWithNullLikeBlankUserEid() {
        store.writeRows(CsvAdminRepository.FILE, CsvAdminRepository.HEADER, List.of(
                new String[]{"1", "", "Alice", "pw"},
                new String[]{"2", "e002", "Bob", "pw2"}
        ));

        assertTrue(repo.findByUserEID("e001").isEmpty());
        assertTrue(repo.findByUserEID("e002").isPresent());
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

    @Test
    void deleteById_RemovesOnlyMatchingAdmin() {
        Admin first = repo.save(buildAdmin(0, "e001", "Alice", "pw1"));
        Admin second = repo.save(buildAdmin(0, "e002", "Bob", "pw2"));

        repo.deleteById(first.getStaffId());

        assertTrue(repo.findById(first.getStaffId()).isEmpty());
        assertEquals("Bob", repo.findById(second.getStaffId()).orElseThrow().getUserName());
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

    @Test
    void save_NullFields_ArePersistedAsEmptyStrings() {
        Admin saved = repo.save(buildAdmin(0, null, null, null));

        Admin found = repo.findById(saved.getStaffId()).orElseThrow();
        assertEquals("", found.getUserEID());
        assertEquals("", found.getUserName());
        assertEquals("", found.getPassword());
    }

    @Test
    void save_ExistingAdmin_PreservesOtherAdmins() {
        Admin first = repo.save(buildAdmin(0, "e001", "Alice", "pw1"));
        Admin second = repo.save(buildAdmin(0, "e002", "Bob", "pw2"));

        repo.save(buildAdmin(first.getStaffId(), "e001", "Alice Updated", "newpw"));

        List<Admin> all = repo.findAll();
        assertEquals(2, all.size());
        assertEquals("Alice Updated", repo.findById(first.getStaffId()).orElseThrow().getUserName());
        assertEquals("Bob", repo.findById(second.getStaffId()).orElseThrow().getUserName());
    }

    // ── persistence across instances ──────────────────────────────────────────

    @Test
    void savedData_PersistedAcrossRepoInstances() {
        CsvAdminRepository first = new CsvAdminRepository(store, idGen);
        first.save(buildAdmin(0, "e001", "Alice", "pw"));

        // New repo instance reading from same directory
        CsvAdminRepository second = new CsvAdminRepository(store, new CsvIdGenerator(store));
        assertEquals(1, second.findAll().size());
        assertTrue(second.findByUserEID("e001").isPresent());
    }
}
