package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    private int enrollCapacity;
    private int waitlistCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private Type type;

    private String venue;

    @ManyToOne(optional = false)
    private Course course;

    // @ManyToMany private Set<Student> enrolledStudents;
    // @ManyToMany private Set<Student> waitlistedStudents;

    public Section() {}

    /** Legacy 10-arg constructor used in tests. */
    public Section(
            int sectionId,
            int enrollCapacity,
            int waitlistCapacity,
            int minStudents,
            String typeStr,
            String venue,
            Course course,
            int extraParam,
            Set<Student> enrolledStudents,
            Set<Student> waitlistedStudents) {
        this.sectionId = sectionId;
        this.enrollCapacity = enrollCapacity;
        this.waitlistCapacity = waitlistCapacity;
        this.venue = venue;
    }

    public Section(
            int sectionId,
            int enrollCapacity,
            int waitlistCapacity,
            Type type,
            String venue,
            Course course,
            Set<Student> enrolledStudents,
            Set<Student> waitlistedStudents) {
        this.sectionId = sectionId;
        this.enrollCapacity = enrollCapacity;
        this.waitlistCapacity = waitlistCapacity;
        this.type = type;
        this.venue = venue;
        // this.course = course;
        // this.enrolledStudents = enrolledStudents;
        // this.waitlistedStudents = waitlistedStudents;
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

    public void enrollStudent(Student student) {
        throw new UnsupportedOperationException();
    }

    public void waitlistStudent(Student student) {
        throw new UnsupportedOperationException();
    }

    // public Course getCourse() {
    //     return course;
    // }

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

    // public void addStudent(Student student) {
    //     if (!canEnroll(student)) {
    //         throw new RuntimeException();
    //     }
    //     this.enrolledStudents.add(student);
    // }
}
