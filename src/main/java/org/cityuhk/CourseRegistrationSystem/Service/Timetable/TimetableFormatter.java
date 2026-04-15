package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;

/**
 * Formatter interface for timetable rows.
 * Separates formatting logic from export logic, enabling different formats.
 * 
 * Applies: Strategy Pattern, Single Responsibility Principle
 */
public interface TimetableFormatter {
    
    /**
     * Formats a registration record as a string row.
     * 
     * @param record the registration record to format
     * @return formatted row string, or null if record cannot be formatted
     */
    String formatRow(RegistrationRecord record);
    
    /**
     * Formats the header for the timetable.
     * 
     * @return formatted header string
     */
    String formatHeader();
    
    /**
     * Formats the timetable title section (student ID, generation time, etc.).
     * 
     * @param studentId the student ID
     * @return formatted title section
     */
    String formatTitle(Integer studentId);
}
