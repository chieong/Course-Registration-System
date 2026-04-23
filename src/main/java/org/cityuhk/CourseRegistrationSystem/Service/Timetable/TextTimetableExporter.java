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

/**
 * Text-based implementation of TimetableExporter.
 * Exports timetable to a formatted text file.
 * 
 * Applies: Concrete Strategy implementation, Dependency on abstraction (TimetableFormatter)
 */
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
        
        if (timetableData.getSections().isEmpty()) {
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
            // Write title section
            writer.write(formatter.formatTitle(timetableData.getOwnerId(), timetableData.getUserType()));
            
            // Write header
            writer.write(formatter.formatHeader());
            
            // Write sorted registration records
            List<Section> sections = new ArrayList<>(timetableData.getSections());
            Collections.sort(sections);
            
            for (Section section : sections) {
                String row = formatter.formatRow(section);
                if (row != null) {
                    writer.write(row);
                    writer.newLine();
                }
            }
        }
        
        return outputPath;
    }
}
