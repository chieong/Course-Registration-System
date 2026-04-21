package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Concrete formatter for text-based timetable output.
 * Produces formatted columns suitable for display and printing.
 * 
 * Applies: Concrete Strategy implementation, Single Responsibility
 */
@Component
public class TextTimetableFormatter implements TimetableFormatter {
    
    private static final String SEPARATOR_LINE = "----------------------------------------------------------------------";
    private static final String TITLE_FORMAT = "%-30s | %s";
    private static final String HEADER_FORMAT = "%-6s %-13s %-12s %-8s %-18s %-22s";
    private static final String ROW_FORMAT = "%-6s %-13s %-12s %-8s %-18s %-22s";
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @Override
    public String formatTitle(Integer studentId) {
        StringBuilder title = new StringBuilder();
        title.append("STUDENT TIMETABLE\n");
        title.append(String.format("Student ID: %d\n", studentId));
        title.append(String.format("Generated At: %s\n", LocalDateTime.now().format(dateFormatter)));
        title.append("\n");
        return title.toString();
    }
    
    @Override
    public String formatHeader() {
        StringBuilder header = new StringBuilder();
        header.append(String.format(HEADER_FORMAT, "DAY", "TIME", "COURSE", "SEC", "TYPE", "VENUE"));
        header.append("\n");
        header.append(SEPARATOR_LINE);
        header.append("\n");
        return header.toString();
    }
    
    @Override
    public String formatRow(RegistrationRecord record) {
        if (record == null || record.getSection() == null) {
            return null;
        }
        
        String courseCode = record.getSection().getCourse() != null 
            ? record.getSection().getCourse().getCourseCode() 
            : "";
        String sectionType = record.getSection().getType() != null 
            ? record.getSection().getType().name() 
            : "";
        String venue = record.getSection().getVenue() != null 
            ? record.getSection().getVenue() 
            : "";
        
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String day = record.getSection().getStartTime() != null 
            ? record.getSection().getStartTime().format(dayFormatter) 
            : "N/A";
        String timeRange = (record.getSection().getStartTime() != null && record.getSection().getEndTime() != null)
            ? record.getSection().getStartTime().format(timeFormatter) + "-" + record.getSection().getEndTime().format(timeFormatter)
            : "N/A";
        
        return String.format(
            ROW_FORMAT,
            day,
            timeRange,
            trimToWidth(courseCode, 12),
            record.getSection().getSectionID(),
            trimToWidth(sectionType, 18),
            trimToWidth(venue, 22));
    }
    
    private String trimToWidth(String value, int width) {
        if (value == null) {
            return "";
        }
        if (value.length() <= width) {
            return value;
        }
        return value.substring(0, Math.max(0, width - 3)) + "...";
    }
}
