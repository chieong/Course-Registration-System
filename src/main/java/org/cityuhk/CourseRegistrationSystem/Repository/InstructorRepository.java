package org.cityuhk.CourseRegistrationSystem.Repository;

import java.util.Optional;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstructorRepository extends JpaRepository<Instructor, Integer> {
	@Query("select i from Instructor i where lower(i.UserEID) = lower(:userEID)")
	Optional<Instructor> findByUserEID(@Param("userEID") String userEID);
}
