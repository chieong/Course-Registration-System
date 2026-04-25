package org.cityuhk.CourseRegistrationSystem.Cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CliSessionStoreTest {

    @TempDir
    Path tempDir;

    static class TestCliSessionStore extends CliSessionStore {
        private final Path path;

        TestCliSessionStore(Path path) {
            this.path = path;
        }

        @Override
        Path getSessionFilePath() {
            return path;
        }
    }

    @Test
    void getSessionFilePathUsesUserHomeAndExpectedFileName() {
        CliSessionStore store = new CliSessionStore();
        Path path = store.getSessionFilePath();

        assertEquals(".course-registration-cli-session.properties", path.getFileName().toString());
    }

    @Test
    void loadReturnsEmptyWhenFileDoesNotExist() {
        Path file = tempDir.resolve("missing.properties");
        CliSessionStore store = new TestCliSessionStore(file);

        Optional<CliSession> loaded = store.load();

        assertTrue(loaded.isEmpty());
    }

    @Test
    void saveAndLoadRoundTripWorks() throws Exception {
        Path file = tempDir.resolve("session.properties");
        CliSessionStore store = new TestCliSessionStore(file);

        CliSession session = new CliSession("admin01", CliRole.ADMIN);
        store.save(session);

        Optional<CliSession> loaded = store.load();

        assertTrue(loaded.isPresent());
        assertEquals("admin01", loaded.get().getUserEid());
        assertEquals(CliRole.ADMIN, loaded.get().getRole());
    }

    @Test
    void loadReturnsEmptyWhenPropertiesMissingUserEidAndRole() throws Exception {
        Path file = tempDir.resolve("session.properties");
        Files.writeString(file, "somethingElse=value\n");
        CliSessionStore store = new TestCliSessionStore(file);

        Optional<CliSession> loaded = store.load();

        assertTrue(loaded.isEmpty());
    }

    @Test
    void loadReturnsEmptyWhenUserEidIsMissing() throws Exception {
        Path file = tempDir.resolve("session.properties");
        Files.writeString(file, "role=ADMIN\n");
        CliSessionStore store = new TestCliSessionStore(file);

        Optional<CliSession> loaded = store.load();

        assertTrue(loaded.isEmpty());
    }

    @Test
    void loadReturnsEmptyWhenRoleIsMissing() throws Exception {
        Path file = tempDir.resolve("session.properties");
        Files.writeString(file, "userEid=testuser\n");
        CliSessionStore store = new TestCliSessionStore(file);

        Optional<CliSession> loaded = store.load();

        assertTrue(loaded.isEmpty());
    }

    @Test
    void loadReturnsEmptyWhenRoleIsInvalid() throws Exception {
        Path file = tempDir.resolve("session.properties");
        Files.writeString(file, "userEid=testuser\nrole=NOT_A_ROLE\n");
        CliSessionStore store = new TestCliSessionStore(file);

        Optional<CliSession> loaded = store.load();

        assertTrue(loaded.isEmpty());
    }

    @Test
    void loadReturnsEmptyOnIOException() throws Exception {
        Path directory = tempDir.resolve("dirAsSessionFile");
        Files.createDirectories(directory);

        CliSessionStore store = new TestCliSessionStore(directory);

        Optional<CliSession> loaded = store.load();

        assertTrue(loaded.isEmpty());
    }

    @Test
    void clearDeletesSessionFileIfExists() throws Exception {
        Path file = tempDir.resolve("session.properties");
        CliSessionStore store = new TestCliSessionStore(file);

        store.save(new CliSession("student01", CliRole.STUDENT));
        assertTrue(Files.exists(file));

        store.clear();

        assertFalse(Files.exists(file));
    }

    @Test
    void clearDoesNothingWhenFileDoesNotExist() throws IOException {
        Path file = tempDir.resolve("no-file.properties");
        CliSessionStore store = new TestCliSessionStore(file);

        store.clear();

        assertFalse(Files.exists(file));
    }
}
