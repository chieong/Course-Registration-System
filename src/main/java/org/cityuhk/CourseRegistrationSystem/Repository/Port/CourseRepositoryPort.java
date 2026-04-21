package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import java.util.List;
import java.util.Optional;

public interface CourseRepositoryPort {
    Optional<Course> findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);
    Course getCourseByCourseCode(String courseCode);
    Course save(Course course);
    void delete(Course course);
    List<Course> findAll();
}
