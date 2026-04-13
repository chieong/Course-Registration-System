package org.cityuhk.CourseRegistrationSystem.Repository;

import java.util.Optional;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
	Optional<Student> findByUserEID(String userEID);
}
