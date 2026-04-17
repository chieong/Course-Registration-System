package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
}
