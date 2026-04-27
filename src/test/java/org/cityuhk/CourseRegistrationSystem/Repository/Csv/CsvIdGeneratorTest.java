package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvIdGeneratorTest {

    @TempDir
    Path tempDir;

    private CsvFileStore store;
    private CsvIdGenerator idGen;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
        idGen = new CsvIdGenerator(store);
    }

    @Test
    void nextId_FirstCallReturns1() {
        assertEquals(1, idGen.nextId("admin"));
    }

    @Test
    void nextId_SequentialCallsIncrement() {
        assertEquals(1, idGen.nextId("admin"));
        assertEquals(2, idGen.nextId("admin"));
        assertEquals(3, idGen.nextId("admin"));
    }

    @Test
    void nextId_DifferentEntitiesHaveIndependentSequences() {
        assertEquals(1, idGen.nextId("admin"));
        assertEquals(1, idGen.nextId("student"));
        assertEquals(2, idGen.nextId("admin"));
        assertEquals(2, idGen.nextId("student"));
    }

    @Test
    void nextId_PersistedAcrossRestarts() {
        idGen.nextId("admin"); // 1
        idGen.nextId("admin"); // 2

        // Create a new generator backed by the same store (simulates restart)
        CsvIdGenerator idGen2 = new CsvIdGenerator(store);
        assertEquals(3, idGen2.nextId("admin"));
    }

    @Test
    void nextId_NewEntityStartsAt1AfterRestart() {
        idGen.nextId("admin"); 
        CsvIdGenerator idGen2 = new CsvIdGenerator(store);
        assertEquals(1, idGen2.nextId("course")); // course starts fresh
    }
}
