package org.cityuhk.CourseRegistrationSystem.Cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CliCommandParser {

    private CliCommandParser() {
    }

    static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null || line.isBlank()) {
            return tokens;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (Character.isWhitespace(ch) && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            current.append(ch);
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        if (inQuotes) {
            throw new IllegalArgumentException("Unclosed quote in command");
        }

        return tokens;
    }

    static Map<String, String> parseOptions(List<String> args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 0; i < args.size(); i++) {
            String token = args.get(i);
            if (!token.startsWith("--")) {
                throw new IllegalArgumentException("Expected option starting with --, got: " + token);
            }

            String key = token.substring(2).trim().toLowerCase();
            if (key.isEmpty()) {
                throw new IllegalArgumentException("Option key cannot be empty");
            }

            if (i + 1 >= args.size()) {
                throw new IllegalArgumentException("Missing value for option --" + key);
            }

            int valueStart = i + 1;
            if (args.get(valueStart).startsWith("--")) {
                throw new IllegalArgumentException("Missing value for option --" + key);
            }

            StringBuilder valueBuilder = new StringBuilder();
            int j = valueStart;
            while (j < args.size() && !args.get(j).startsWith("--")) {
                if (valueBuilder.length() > 0) {
                    valueBuilder.append(' ');
                }
                valueBuilder.append(args.get(j));
                j++;
            }

            String value = valueBuilder.toString();
            options.put(key, value);
            i = j - 1;
        }
        return options;
    }
}
