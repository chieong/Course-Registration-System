package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles atomic reads and writes of CSV files backed by a base directory.
 * Writes are performed atomically via a temp file + rename strategy.
 */
public class CsvFileStore {

    private final Path baseDir;

    public CsvFileStore(String csvDir) {
        this.baseDir = Path.of(csvDir);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create CSV directory: " + csvDir, e);
        }
    }

    public Path getBaseDir() {
        return baseDir;
    }

    /**
     * Reads all data rows (skipping the header) from a CSV file.
     * Returns an empty list if the file does not exist.
     */
    public List<String[]> readRows(String filename) {
        Path file = baseDir.resolve(filename);
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // skip header
                    continue;
                }
                if (!line.isBlank()) {
                    rows.add(parseLine(line));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + filename, e);
        }
        return rows;
    }

    /**
     * Writes all rows (with header) to a CSV file atomically.
     */
    public void writeRows(String filename, String[] header, List<String[]> rows) {
        Path file = baseDir.resolve(filename);
        Path tmp = baseDir.resolve(filename + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(joinLine(header));
            writer.newLine();
            for (String[] row : rows) {
                writer.write(joinLine(row));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing CSV file: " + filename, e);
        }
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error renaming temp CSV file: " + filename, e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error renaming temp CSV file: " + filename, e);
        }
    }

    /** Parse a single CSV line (handles quoted fields). */
    public static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /** Joins fields into a CSV line with proper quoting. */
    public static String joinLine(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(fields[i]));
        }
        return sb.toString();
    }

    public static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
