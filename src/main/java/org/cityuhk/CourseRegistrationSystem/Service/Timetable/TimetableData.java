package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Student;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for timetable information.
 * Immutable holder of timetable details with builder pattern support.
 * 
 * Applies: Builder Pattern for flexible construction, Immutability principle
 */
public class TimetableData {
    private final Integer studentId;
    private final Student student;
    private final List<RegistrationRecord> registrationRecords;
    private final DateTimeFormatter dayFormatter;
    private final DateTimeFormatter timeFormatter;

    private TimetableData(Builder builder) {
        this.studentId = builder.studentId;
        this.student = builder.student;
        this.registrationRecords = Collections.unmodifiableList(builder.registrationRecords);
        this.dayFormatter = builder.dayFormatter;
        this.timeFormatter = builder.timeFormatter;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public Student getStudent() {
        return student;
    }

    public List<RegistrationRecord> getRegistrationRecords() {
        return registrationRecords;
    }

    public DateTimeFormatter getDayFormatter() {
        return dayFormatter;
    }

    public DateTimeFormatter getTimeFormatter() {
        return timeFormatter;
    }

    @Override
    public String toString() {
        return "TimetableData{" +
                "studentId=" + studentId +
                ", recordCount=" + registrationRecords.size() +
                '}';
    }

    /**
     * Builder for flexible TimetableData construction.
     * Follows the Builder pattern to handle multiple optional parameters.
     */
    public static class Builder {
        private Integer studentId;
        private Student student;
        private List<RegistrationRecord> registrationRecords = Collections.emptyList();
        private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        public Builder studentId(Integer studentId) {
            this.studentId = Objects.requireNonNull(studentId, "Student ID cannot be null");
            return this;
        }

        public Builder student(Student student) {
            this.student = student;
            return this;
        }

        public Builder registrationRecords(List<RegistrationRecord> records) {
            this.registrationRecords = Objects.requireNonNull(records, "Registration records cannot be null");
            return this;
        }

        public Builder dayFormatter(DateTimeFormatter formatter) {
            this.dayFormatter = Objects.requireNonNull(formatter, "Day formatter cannot be null");
            return this;
        }

        public Builder timeFormatter(DateTimeFormatter formatter) {
            this.timeFormatter = Objects.requireNonNull(formatter, "Time formatter cannot be null");
            return this;
        }

        public TimetableData build() {
            if (studentId == null) {
                throw new IllegalStateException("Student ID is required");
            }
            if (registrationRecords.isEmpty()) {
                throw new IllegalStateException("Registration records cannot be empty");
            }
            return new TimetableData(this);
        }
    }
}
