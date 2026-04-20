package org.cityuhk.CourseRegistrationSystem.Cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
class CliSessionStore {

    private static final String FILE_NAME = ".course-registration-cli-session.properties";

    Path getSessionFilePath() {
        return Paths.get(System.getProperty("user.home"), FILE_NAME);
    }

    Optional<CliSession> load() {
        Path path = getSessionFilePath();
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
            String userEid = properties.getProperty("userEid");
            String role = properties.getProperty("role");
            if (userEid == null || role == null) {
                return Optional.empty();
            }
            return Optional.of(new CliSession(userEid, CliRole.valueOf(role)));
        } catch (IOException | IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    void save(CliSession session) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("userEid", session.getUserEid());
        properties.setProperty("role", session.getRole().name());

        Path path = getSessionFilePath();
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            properties.store(outputStream, "Course Registration CLI Session");
        }
    }

    void clear() throws IOException {
        Files.deleteIfExists(getSessionFilePath());
    }
}
