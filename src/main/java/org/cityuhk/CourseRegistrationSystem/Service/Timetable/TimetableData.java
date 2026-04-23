package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Section;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;


public class TimetableData {
    private final Integer ownerId;
    private final UserType userType;
    private final Set<Section> sections;
    private final DateTimeFormatter dayFormatter;
    private final DateTimeFormatter timeFormatter;

    public enum UserType{
        Student,Instructor
    }

    private TimetableData(Builder builder) {
        this.ownerId = builder.ownerId;
        this.userType = builder.userType;
        this.dayFormatter = builder.dayFormatter;
        this.timeFormatter = builder.timeFormatter;
        this.sections = builder.sections;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public UserType getUserType() {
        return userType;
    }

    public Set<Section> getSections() {
        return sections;
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
                "ownerId=" + ownerId +
                ", recordCount=" + sections.size() +
                '}';
    }

    /**
     * Builder for flexible TimetableData construction.
     * Follows the Builder pattern to handle multiple optional parameters.
     */
    public static class Builder {
        private Integer ownerId;
        private UserType userType;
        private Set<Section> sections;
        private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        public Builder ownerId(Integer ownerId) {
            this.ownerId = Objects.requireNonNull(ownerId, "owner ID cannot be null");
            return this;
        }

        public Builder userType(UserType userType) {
            this.userType = userType;
            return this;
        }

        public Builder sections(Set<Section> sections) {
            this.sections = sections;
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
            if (ownerId == null) {
                throw new IllegalStateException("Student ID is required");
            }
            if(userType == null) {
                throw new IllegalStateException("UserType is required");
            }
            return new TimetableData(this);
        }
    }
}
