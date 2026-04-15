package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.cityuhk.CourseRegistrationSystem.Service.Semester;

import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/** which student is enrolled to which section */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"student_student_id", "section_section_id"})})
public class RegistrationRecord implements Comparable<RegistrationRecord> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer recordId;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Section section;

    private LocalDateTime timestamp;

    public RegistrationRecord() {}

    public RegistrationRecord(Student student, Section section, LocalDateTime timestamp) {
        this.student = student;
        this.section = section;
        this.timestamp = timestamp;
    }

    public Student getStudent() {
        return student;
    }

    public Section getSection() {
        return section;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean within(Semester semester) {
        return timestamp.isBefore(semester.start()) && timestamp.isAfter(semester.end());
    }

    public int addCredits(int sum) {
        return section.addCredits(sum);
    }

    @Override
    public int compareTo(RegistrationRecord other) {
        return Comparator
            .comparing(RegistrationRecord::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(RegistrationRecord::getEndTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .compare(this, other);
    }

    public LocalDateTime getStartTime() { 
        return section != null ? section.getStartTime() : null;
    }

    public LocalDateTime getEndTime() {
        return section != null ? section.getEndTime() : null;
    }

    public String toTimetableRow(DateTimeFormatter dayFormatter, DateTimeFormatter timeFormatter) {
        if (this.section == null) {
            return null; // Skip invalid records
        }

        String courseCode = section.getCourse() != null ? section.getCourse().getCourseCode() : "";
        String sectionType = section.getType() != null ? section.getType().name() : "";
        String venue = section.getVenue() != null ? section.getVenue() : "";

        String day = section.getStartTime() != null ? section.getStartTime().format(dayFormatter) : "N/A";
        String timeRange = (section.getStartTime() != null && section.getEndTime() != null)
                ? section.getStartTime().format(timeFormatter) + "-" + section.getEndTime().format(timeFormatter)
                : "N/A";

        return String.format(
                "%-6s %-13s %-12s %-8s %-18s %-22s",
                day,
                timeRange,
                trimToWidth(courseCode, 12),
                section.getSectionID(),
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
