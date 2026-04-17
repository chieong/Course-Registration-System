package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Course { // for creating a course

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseId;

    private String courseCode;
    private String title;
    private int credits;
    private String description;

    @ManyToMany
    @JoinTable(
            name = "course_prerequisite",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id"))
        private Set<Course> prerequisiteCourses = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "course_exclusive",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "exclusivve_id"))
        private Set<Course> exclusiveCourses = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Section> sections = new HashSet<>();

    private String term;

    public Course() {}

    /** Constructor without a generated ID; used in tests and ad-hoc construction. */
    public Course(
            String courseCode,
            String title,
            int credits,
            String description,
            String term,
            Set<Course> prerequisiteCourses,
            Set<Course> exclusiveCourses,
            Set<Section> sections) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.description = description;
        this.term = term;
        this.prerequisiteCourses = prerequisiteCourses != null ? new HashSet<>(prerequisiteCourses) : new HashSet<>();
        this.exclusiveCourses = exclusiveCourses != null ? new HashSet<>(exclusiveCourses) : new HashSet<>();
        this.sections = sections != null ? new HashSet<>(sections) : new HashSet<>();
    }

    // getter
    public Integer getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Course> getPrerequisiteCourses() {
        return prerequisiteCourses;
    }

    public void setPrerequisiteCourses(Set<Course> prerequisiteCourses) {
        this.prerequisiteCourses = prerequisiteCourses != null ? prerequisiteCourses : new HashSet<>();
    }

    public Set<Course> getExclusiveCourses() {
        return exclusiveCourses;
    }

    public void setExclusiveCourses(Set<Course> exclusiveCourses) {
        this.exclusiveCourses = exclusiveCourses != null ? exclusiveCourses : new HashSet<>();
    }

    public Set<Section> getSections() {
        return sections;
    }

    public void setSections(Set<Section> sections) {
        this.sections = sections != null ? sections : new HashSet<>();
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public boolean satisfyPrerequisites(Set<Course> enrolledCourses) {
        return enrolledCourses.containsAll(prerequisiteCourses);
    }

    public boolean notTakenExclusives(Set<Course> enrolledCourses) {
        return Collections.disjoint(enrolledCourses, exclusiveCourses);
    }

    public boolean hasCredits(Student student) {
        return student.hasCredits(credits);
    }

    public int addCredits(int sum) {
        return sum + credits;
    }
}
