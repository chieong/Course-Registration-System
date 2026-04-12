package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

public class ExclusiveCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne private Course courseA;
    @ManyToOne private Course courseB;

    public ExclusiveCourse() {}

    public ExclusiveCourse(Integer id, Course courseA, Course courseB) {
        this.id = id;
        this.courseA = courseA;
        this.courseB = courseB;
    }
}
