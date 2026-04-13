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

/** which student is enrolled to which section */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"student_student_id", "section_section_id"})})
public class RegistrationRecord {
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

    public boolean within(Semester semester) {
        return timestamp.isBefore(semester.start()) && timestamp.isAfter(semester.end());
    }

    public int addCredits(int sum) {
        return section.addCredits(sum);
    }
}
