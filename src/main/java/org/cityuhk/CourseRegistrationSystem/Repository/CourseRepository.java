package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Integer> {
}
