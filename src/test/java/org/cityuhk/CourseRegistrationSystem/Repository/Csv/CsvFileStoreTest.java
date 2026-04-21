package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvFileStoreTest {

    @TempDir
    Path tempDir;

    private CsvFileStore store;

    @BeforeEach
    void setUp() {
        store = new CsvFileStore(tempDir.toString());
    }

    // ── readRows ──────────────────────────────────────────────────────────────

    @Test
    void readRows_FileAbsent_ReturnsEmptyList() {
        List<String[]> rows = store.readRows("nonexistent.csv");
        assertTrue(rows.isEmpty());
    }

    @Test
    void readRows_SkipsHeaderReturnsDataRows() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "col1,col2\nval1,val2\nval3,val4\n");

        List<String[]> rows = store.readRows("test.csv");

        assertEquals(2, rows.size());
        assertArrayEquals(new String[]{"val1", "val2"}, rows.get(0));
        assertArrayEquals(new String[]{"val3", "val4"}, rows.get(1));
    }

    @Test
    void readRows_IgnoresBlankLines() throws IOException {
        Path file = tempDir.resolve("blank.csv");
        Files.writeString(file, "h1,h2\n\nval1,val2\n\n");

        List<String[]> rows = store.readRows("blank.csv");

        assertEquals(1, rows.size());
        assertArrayEquals(new String[]{"val1", "val2"}, rows.get(0));
    }

    // ── writeRows / round-trip ────────────────────────────────────────────────

    @Test
    void writeRows_CreatesFileWithHeaderAndData() {
        String[] header = {"id", "name"};
        List<String[]> data = List.of(
                new String[]{"1", "Alice"},
                new String[]{"2", "Bob"}
        );

        store.writeRows("people.csv", header, data);

        List<String[]> read = store.readRows("people.csv");
        assertEquals(2, read.size());
        assertArrayEquals(new String[]{"1", "Alice"}, read.get(0));
        assertArrayEquals(new String[]{"2", "Bob"}, read.get(1));
    }

    @Test
    void writeRows_OverwritesPreviousContent() {
        String[] header = {"id"};
        store.writeRows("over.csv", header, java.util.Collections.singletonList(new String[]{"1"}));
        store.writeRows("over.csv", header, java.util.Collections.singletonList(new String[]{"99"}));

        List<String[]> rows = store.readRows("over.csv");
        assertEquals(1, rows.size());
        assertEquals("99", rows.get(0)[0]);
    }

    @Test
    void writeRows_EmptyDataWritesOnlyHeader() {
        store.writeRows("empty.csv", new String[]{"col"}, java.util.Collections.emptyList());

        List<String[]> rows = store.readRows("empty.csv");
        assertTrue(rows.isEmpty());
    }

    // ── parseLine ─────────────────────────────────────────────────────────────

    @Test
    void parseLine_SimpleValues() {
        assertArrayEquals(new String[]{"a", "b", "c"}, CsvFileStore.parseLine("a,b,c"));
    }

    @Test
    void parseLine_QuotedFieldContainingComma() {
        assertArrayEquals(new String[]{"hello, world", "x"}, CsvFileStore.parseLine("\"hello, world\",x"));
    }

    @Test
    void parseLine_EscapedDoubleQuoteInsideQuotedField() {
        assertArrayEquals(new String[]{"say \"hi\"", "ok"}, CsvFileStore.parseLine("\"say \"\"hi\"\"\",ok"));
    }

    @Test
    void parseLine_EmptyField() {
        String[] result = CsvFileStore.parseLine("a,,c");
        assertArrayEquals(new String[]{"a", "", "c"}, result);
    }

    // ── joinLine / escapeCsv ──────────────────────────────────────────────────

    @Test
    void joinLine_PlainValues_NoQuoting() {
        assertEquals("a,b,c", CsvFileStore.joinLine(new String[]{"a", "b", "c"}));
    }

    @Test
    void joinLine_ValueContainingComma_IsQuoted() {
        String line = CsvFileStore.joinLine(new String[]{"hello, world"});
        assertEquals("\"hello, world\"", line);
    }

    @Test
    void joinLine_ValueContainingDoubleQuote_IsEscaped() {
        String line = CsvFileStore.joinLine(new String[]{"say \"hi\""});
        assertEquals("\"say \"\"hi\"\"\"", line);
    }

    @Test
    void parseLine_RoundTrip_PreservesSpecialChars() {
        String[] original = {"id with,comma", "quote\"here", "normal"};
        String line = CsvFileStore.joinLine(original);
        assertArrayEquals(original, CsvFileStore.parseLine(line));
    }
}
