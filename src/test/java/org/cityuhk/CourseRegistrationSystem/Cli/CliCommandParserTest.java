package org.cityuhk.CourseRegistrationSystem.Cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CliCommandParserTest {

    @Test
    void tokenizeSupportsQuotedArgs() {
        List<String> tokens = CliCommandParser.tokenize("admin-create-user admin01 \"System Admin\" s3cret");
        assertEquals(List.of("admin-create-user", "admin01", "System Admin", "s3cret"), tokens);
    }

    @Test
    void tokenizeThrowsOnUnclosedQuote() {
        assertThrows(IllegalArgumentException.class, () -> CliCommandParser.tokenize("login \"admin"));
    }

    @Test
    void parseOptionsParsesPairs() {
        Map<String, String> options = CliCommandParser.parseOptions(List.of("--code", "CS101", "--credits", "3"));
        assertEquals("CS101", options.get("code"));
        assertEquals("3", options.get("credits"));
    }

    @Test
    void parseOptionsThrowsOnMissingValue() {
        assertThrows(IllegalArgumentException.class, () -> CliCommandParser.parseOptions(List.of("--code")));
    }
}
