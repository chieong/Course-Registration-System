package org.cityuhk.CourseRegistrationSystem.Repository;

import java.util.Optional;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Integer> {
	@Query("select s from Student s where lower(s.UserEID) = lower(:userEID)")
	Optional<Student> findByUserEID(@Param("userEID") String userEID);
}
