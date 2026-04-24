package org.cityuhk.CourseRegistrationSystem.RestController.dto;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;

public class studentListResponse {
    private Student student;
    private Section section;
    private Course course;

    public studentListResponse(Student student, Section section, Course course) {
        this.student = student;
        this.section = section;
        this.course = course;
    }

    public Student getStudent() {
        return student;
    }

    public Section getSection() {
        return section;
    }

    public Course getCourse() {
        return course;
    }
}
