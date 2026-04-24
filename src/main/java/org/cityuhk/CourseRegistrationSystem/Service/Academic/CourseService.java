package org.cityuhk.CourseRegistrationSystem.Service.Academic;

import org.cityuhk.CourseRegistrationSystem.Repository.Port.CourseRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {
    public final CourseRepositoryPort courseRepository;

    @Autowired
    public CourseService(CourseRepositoryPort courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course getCourse(String courseCode) {
        return courseRepository.getCourseByCourseCode(courseCode);
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAllWithSections();
    }

    public List<Course> getAllCoursesWithAllData() {
        return courseRepository.findAllWithAllData();
    }
}
