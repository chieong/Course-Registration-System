package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
        
        if (timetableData.getRegistrationRecords().isEmpty()) {
            throw new TimetableExportException("No registration records to export");
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
            "student-" + timetableData.getStudentId() + "-timetable-", 
            getFileExtension());
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            // Write title section
            writer.write(formatter.formatTitle(timetableData.getStudentId()));
            
            // Write header
            writer.write(formatter.formatHeader());
            
            // Write sorted registration records
            List<RegistrationRecord> sortedRecords = new java.util.ArrayList<>(timetableData.getRegistrationRecords());
            Collections.sort(sortedRecords);
            
            for (RegistrationRecord record : sortedRecords) {
                String row = formatter.formatRow(record);
                if (row != null) {
                    writer.write(row);
                    writer.newLine();
                }
            }
        }
        
        return outputPath;
    }
}
