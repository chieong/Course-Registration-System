package org.cityuhk.CourseRegistrationSystem.Persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Converter
public class LocalDateTimeStringConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter SQLITE_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SQLITE_SPACE_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        long epochMillis = attribute.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return String.valueOf(epochMillis);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();

        // Backward compatibility for old SQLite rows that saved epoch millis as text.
        if (value.matches("^-?\\d+$")) {
            try {
                long epochMillis = Long.parseLong(value);
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
            } catch (NumberFormatException ignored) {
                // Continue to formatter-based parsing below.
            }
        }

        try {
            return LocalDateTime.parse(value, ISO);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(value, SQLITE_SPACE_MILLIS);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(value, SQLITE_SPACE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Unsupported date-time value in database: " + dbData, ex);
        }
    }
}
