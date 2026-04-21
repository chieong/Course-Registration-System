package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table
public class Section {
    // A section under a course like lab, lecture..etc

    public enum Type {
        LECTURE,
        TUTORIAL,
        LAB,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sectionId;

    @ManyToOne(optional = false)
    private Course course;

    private Integer enrollCapacity;
    private Integer waitlistCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String venue;

    @Enumerated(EnumType.STRING)
    private Type type;



    public Section() {}

    public Section(
            Course code,
            int enrollCapacity,
            int waitlistCapacity,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String venue
            ) {
        this.course = code;
        this.enrollCapacity = enrollCapacity;
        this.waitlistCapacity = waitlistCapacity;
        this.startTime = startTime;
        this.endTime = endTime;
        if((startTime != null && endTime != null) && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Start time cannot be before end time");
        }
        this.venue = venue;
    }

    public boolean canEnroll(Student student, int enrolled) {
        return !isFull(enrolled)
                && hasCredits(student)
                && student.satisfyPrerequisites(course)
                && student.notTakenExclusives(course);
    }

    // getter
    public int getSectionID() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Type getType() {
        return type;
    }

    public String getVenue() {
        return venue;
    }

    public Course getCourse() {
        return course;
    }

    public boolean isFull(int enrolled) {
        return enrolled >= enrollCapacity;
    }

    private boolean hasCredits(Student student) {
        return course.hasCredits(student);
    }

    public boolean overlaps(Section other) {
        return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }

    public int addCredits(int sum) {
        return course.addCredits(sum);
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setTime(LocalDateTime startTime, LocalDateTime endTime) throws IllegalArgumentException {
        this.startTime = startTime;
        this.endTime = endTime;
        if(endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Start time cannot be before end time");
        }
    }

    public void setType(Type type) {
        this.type = type;
    }
    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setEnrollCapacity(int enrollCapacity) {
        this.enrollCapacity = enrollCapacity;
    }

    public void setWaitlistCapacity(int waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }
}
