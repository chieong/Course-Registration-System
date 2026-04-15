package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import java.nio.file.Path;

/**
 * Strategy interface for exporting timetable data in different formats.
 * Enables extensibility for new export formats without modifying existing code.
 * 
 * Applies: Strategy Pattern, Open/Closed Principle, Interface Segregation
 */
public interface TimetableExporter {
    
    /**
     * Exports timetable data to a file in the specific format.
     * 
     * @param timetableData the timetable data to export
     * @return the path to the exported file
     * @throws TimetableExportException if export fails
     */
    Path export(TimetableData timetableData) throws TimetableExportException;
    
    /**
     * Gets the file extension for this export format (e.g., ".txt", ".csv", ".pdf").
     * 
     * @return the file extension including the dot
     */
    String getFileExtension();
    
    /**
     * Gets a human-readable format name.
     * 
     * @return the format name
     */
    String getFormatName();
}
