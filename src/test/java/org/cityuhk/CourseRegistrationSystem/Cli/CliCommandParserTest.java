package org.cityuhk.CourseRegistrationSystem.Cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CliCommandParserTest {

    @Test
    void tokenizeReturnsEmptyListForNullInput() {
        List<String> tokens = CliCommandParser.tokenize(null);
        assertEquals(List.of(), tokens);
    }

    @Test
    void tokenizeReturnsEmptyListForBlankInput() {
        List<String> tokens = CliCommandParser.tokenize("   \t   ");
        assertEquals(List.of(), tokens);
    }

    @Test
    void tokenizeSupportsQuotedArgs() {
        List<String> tokens = CliCommandParser.tokenize("admin-create-user admin01 \"System Admin\" s3cret");
        assertEquals(List.of("admin-create-user", "admin01", "System Admin", "s3cret"), tokens);
    }

    @Test
    void tokenizeHandlesExtraWhitespaceBetweenTokens() {
        List<String> tokens = CliCommandParser.tokenize("login    admin01     password123   ");
        assertEquals(List.of("login", "admin01", "password123"), tokens);
    }

    @Test
    void tokenizeThrowsOnUnclosedQuote() {
        assertThrows(IllegalArgumentException.class,
                () -> CliCommandParser.tokenize("login \"admin"));
    }

    @Test
    void tokenizeParsesPeriodCommandWithAllOptions() {
        List<String> tokens = CliCommandParser.tokenize(
                "admin-create-period --cohort 2024 --start 2026-09-01T00:00 --end 2026-11-30T23:59");
        assertEquals(List.of(
                "admin-create-period",
                "--cohort", "2024",
                "--start", "2026-09-01T00:00",
                "--end", "2026-11-30T23:59"), tokens);
    }

    @Test
    void parseOptionsParsesPairs() {
        Map<String, String> options = CliCommandParser.parseOptions(
                List.of("--code", "CS101", "--credits", "3"));
        assertEquals("CS101", options.get("code"));
        assertEquals("3", options.get("credits"));
    }

    @Test
    void parseOptionsLowercasesKeys() {
        Map<String, String> options = CliCommandParser.parseOptions(
                List.of("--CoDe", "CS101", "--CrEdItS", "3"));
        assertEquals("CS101", options.get("code"));
        assertEquals("3", options.get("credits"));
    }

    @Test
    void parseOptionsParsesAdminCreatePeriodArgs() {
        Map<String, String> options = CliCommandParser.parseOptions(
                List.of("--cohort", "2024", "--start", "2026-09-01T00:00", "--end", "2026-11-30T23:59"));
        assertEquals("2024", options.get("cohort"));
        assertEquals("2026-09-01T00:00", options.get("start"));
        assertEquals("2026-11-30T23:59", options.get("end"));
    }

    @Test
    void parseOptionsSupportsUnquotedValuesWithSpaces() {
        Map<String, String> options = CliCommandParser.parseOptions(
                List.of("--title", "Object", "Oriented", "Programming", "--credits", "3"));
        assertEquals("Object Oriented Programming", options.get("title"));
        assertEquals("3", options.get("credits"));
    }

    @Test
    void parseOptionsThrowsWhenTokenDoesNotStartWithDashDash() {
        assertThrows(IllegalArgumentException.class,
                () -> CliCommandParser.parseOptions(List.of("code", "CS101")));
    }

    @Test
    void parseOptionsThrowsOnEmptyOptionKey() {
        assertThrows(IllegalArgumentException.class,
                () -> CliCommandParser.parseOptions(List.of("--", "value")));
    }

    @Test
    void parseOptionsThrowsOnBlankOptionKeyAfterTrim() {
        assertThrows(IllegalArgumentException.class,
                () -> CliCommandParser.parseOptions(List.of("--   ", "value")));
    }

    @Test
    void parseOptionsThrowsOnMissingValue() {
        assertThrows(IllegalArgumentException.class,
                () -> CliCommandParser.parseOptions(List.of("--code")));
    }

    @Test
    void parseOptionsThrowsOnValueStartingWithDash() {
        assertThrows(IllegalArgumentException.class,
                () -> CliCommandParser.parseOptions(List.of("--cohort", "--start")));
    }

    @Test
    void privateConstructorCanBeInvokedViaReflectionForCoverage() throws Exception {
        Constructor<CliCommandParser> constructor = CliCommandParser.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertDoesNotThrow(() -> constructor.newInstance());
    }
}
