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
    private Set<Course> prerequisiteCourses;

    @ManyToMany
    @JoinTable(
            name = "course_exclusive",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "exclusivve_id"))
    private Set<Course> exclusiveCourses;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Section> sections;

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
            Collection<Section> sections) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.description = description;
        this.term = term;
        this.prerequisiteCourses = prerequisiteCourses;
        this.exclusiveCourses = exclusiveCourses;
        this.sections = sections != null ? new HashSet<>(sections) : null;
    }

    public Course(
            int courseId,
            String courseCode,
            String title,
            int credits,
            String description,
            Set<Course> prerequisiteCourses,
            Set<Section> sections,
            String term) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.description = description;
        this.prerequisiteCourses = prerequisiteCourses;
        this.sections = sections;
        this.term = term;
    }

    // getter
    public int getCredits() {
        return credits;
    }

    public Section getSection(int sectionId) {
        for (Section section : sections) {
            if (section.getSectionID() == sectionId) {
                return section;
            }
        }
        return null;
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
