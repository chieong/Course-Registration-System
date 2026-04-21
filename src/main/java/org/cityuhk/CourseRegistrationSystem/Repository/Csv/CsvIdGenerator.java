package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates unique IDs per entity type, persisted to a sequences file so IDs
 * survive restarts and never collide.
 */
public class CsvIdGenerator {

    private static final String SEQUENCES_FILE = "id_sequences.csv";

    private final CsvFileStore store;
    private final Map<String, Integer> sequences = new ConcurrentHashMap<>();

    public CsvIdGenerator(CsvFileStore store) {
        this.store = store;
        loadSequences();
    }

    /** Returns the next unique ID for the given entity name, then increments. */
    public synchronized int nextId(String entityName) {
        int next = sequences.getOrDefault(entityName, 1);
        sequences.put(entityName, next + 1);
        persistSequences();
        return next;
    }

    private void loadSequences() {
        Path file = store.getBaseDir().resolve(SEQUENCES_FILE);
        if (!Files.exists(file)) return;
        for (String[] row : store.readRows(SEQUENCES_FILE)) {
            if (row.length >= 2) {
                try {
                    sequences.put(row[0], Integer.parseInt(row[1]));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private void persistSequences() {
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        for (Map.Entry<String, Integer> e : sequences.entrySet()) {
            rows.add(new String[]{e.getKey(), String.valueOf(e.getValue())});
        }
        store.writeRows(SEQUENCES_FILE, new String[]{"entity", "nextId"}, rows);
    }
}
