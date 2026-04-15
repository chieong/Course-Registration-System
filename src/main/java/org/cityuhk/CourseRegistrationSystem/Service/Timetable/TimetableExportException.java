package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

/**
 * Custom exception for timetable export operations.
 * Provides clearer error handling for export-specific failures.
 */
public class TimetableExportException extends Exception {
    
    public TimetableExportException(String message) {
        super(message);
    }
    
    public TimetableExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
