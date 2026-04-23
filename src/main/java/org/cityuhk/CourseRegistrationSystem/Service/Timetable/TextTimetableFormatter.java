package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
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
    public String formatTitle(Integer studentId, TimetableData.UserType userType) {
        StringBuilder title = new StringBuilder();
        title.append(userType.toString());
        title.append(" TIMETABLE\n");
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
    public String formatRow(Section section) {
        if (section == null) return "";
        
        String courseCode = section.getCourse() != null
            ? section.getCourse().getCourseCode()
            : "";
        String sectionType = section.getType() != null
            ? section.getType().name()
            : "";
        String venue = section.getVenue() != null
            ? section.getVenue()
            : "";
        
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String day = section.getStartTime() != null
            ? section.getStartTime().format(dayFormatter)
            : "N/A";
        String timeRange = (section.getStartTime() != null && section.getEndTime() != null)
            ? section.getStartTime().format(timeFormatter) + "-" + section.getEndTime().format(timeFormatter)
            : "N/A";
        
        return String.format(
            ROW_FORMAT,
            day,
            timeRange,
            trimToWidth(courseCode, 12),
            section.getSectionId(),
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
