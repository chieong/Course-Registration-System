package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Integer>, InstructorRepositoryPort {
    @Query("select i from Instructor i where lower(i.UserEID) = lower(:userEID)")
    Optional<Instructor> findByUserEID(@Param("userEID") String userEID);
}
