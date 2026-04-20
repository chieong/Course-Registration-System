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

    private int enrollCapacity;
    private int waitlistCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String venue;

    @Enumerated(EnumType.STRING)
    private Type type;



    public Section() {}

    public Section(
            int sectionId,
            int enrollCapacity,
            int waitlistCapacity,
            Type type,
            String venue,
            Course course) {
        this.sectionId = sectionId;
        this.enrollCapacity = enrollCapacity;
        this.waitlistCapacity = waitlistCapacity;
        this.type = type;
        this.venue = venue;
        this.course = course;
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
}
