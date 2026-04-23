package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TextTimetableExporter implements TimetableExporter {

    private final TimetableFormatter formatter;

    public TextTimetableExporter(TextTimetableFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public Path export(TimetableData timetableData) throws TimetableExportException {
        if (timetableData == null) {
            throw new TimetableExportException("Timetable data cannot be null");
        }

        if (timetableData.getSections() == null || timetableData.getSections().isEmpty()) {
            throw new TimetableExportException("No section records to export");
        }

        try {
            return writeToFile(timetableData);
        } catch (Exception ex) {
            throw new TimetableExportException("Failed to export timetable to text format", ex);
        }
    }

    @Override
    public String getFileExtension() {
        return ".txt";
    }

    @Override
    public String getFormatName() {
        return "Text (TXT)";
    }

    private Path writeToFile(TimetableData timetableData) throws Exception {
        Path outputPath = Files.createTempFile(
                timetableData.getUserType().toString() + timetableData.getOwnerId() + "-timetable-",
                getFileExtension());

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(formatter.formatTitle(timetableData));

            writer.write(formatter.formatHeader());

            List<Section> sections = new ArrayList<>(timetableData.getSections());
            Collections.sort(sections);

            for (Section section : sections) {
                String row = formatter.formatRow(section, timetableData);
                if (row != null) {
                    writer.write(row);
                    writer.newLine();
                }
            }
        }

        return outputPath;
    }
}