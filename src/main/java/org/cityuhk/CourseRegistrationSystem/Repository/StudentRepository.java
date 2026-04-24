package org.cityuhk.CourseRegistrationSystem.Repository;

import java.util.Optional;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Integer>, StudentRepositoryPort {
	@Query("select s from Student s where lower(s.userEID) = lower(:userEID)")
	Optional<Student> findByUserEID(@Param("userEID") String userEID);
}
