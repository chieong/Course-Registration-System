package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Comparator;

/** which student is waitlisted to which section */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"student_student_id", "section_section_id"})})
public class WaitlistRecord implements Comparable<WaitlistRecord> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer recordId;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Section section;

    private LocalDateTime timestamp;

    public WaitlistRecord() {}

    public WaitlistRecord(Student student, Section section, LocalDateTime timestamp) {
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

    public int addCredits(int sum) {
        return section.addCredits(sum);
    }

    @Override
    public int compareTo(WaitlistRecord other) {
        return Comparator
            .comparing(WaitlistRecord::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(WaitlistRecord::getEndTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .compare(this, other);
    }

    public LocalDateTime getStartTime() { 
        return section != null ? section.getStartTime() : null;
    }

    public LocalDateTime getEndTime() {
        return section != null ? section.getEndTime() : null;
    }
}
