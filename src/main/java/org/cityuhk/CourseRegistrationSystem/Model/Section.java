package org.cityuhk.CourseRegistrationSystem.Model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table
public class Section implements Comparable<Section>{
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

    @ManyToMany
    @JoinTable(
            name = "section_instructor",
            joinColumns = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "staff_id"))
    private Set<Instructor> instructors = new HashSet<>();

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

    public void assertEnroll(Student student) {
        if (hasCredits(student)) {
            throw new RuntimeException("Student has not enough credits");
        }
        if (!student.satisfyPrerequisites(course)) {
            throw new RuntimeException("Student is not satisfying prerequisites");
        }
        if (!student.notTakenExclusives(course)) {
            throw new RuntimeException("Student has taken exclusives course.");
        }
    }

    public Integer getSectionId() {
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

    public boolean isWaitlistFull(int waitlisted) {
        return waitlisted >= waitlistCapacity;
    }

    public Integer getEnrollCapacity() {
        return enrollCapacity;
    }

    public Integer getWaitlistCapacity() {
        return waitlistCapacity;
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

    public Set<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(Set<Instructor> instructors) {
        this.instructors = instructors != null ? instructors : new HashSet<>();
    }

    public void addInstructor(Instructor instructor) {
        this.instructors.add(instructor);
        instructor.addSection(this);
    }

    public void removeInstructor(Instructor instructor) {
        this.instructors.remove(instructor);
        instructor.removeSection(this);
    }

    @Override
    public int compareTo(Section section) {
        return startTime.compareTo(section.getStartTime());
    }
}
