package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {
	Optional<Course> findByCourseCode(String courseCode);

	@Query("select distinct c from Course c left join fetch c.sections")
	List<Course> findAllWithSections();

	@Query("select case when count(c) > 0 then true else false end from Course c where c.courseCode = :courseCode")
	boolean existsByCourseCode(String courseCode);

    @Query("select c from Course c where c.courseCode = :courseCode")
    Course  getCourseByCourseCode(String courseCode);
}
