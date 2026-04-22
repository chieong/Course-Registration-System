package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import java.util.List;
import java.util.Optional;

public interface InstructorRepositoryPort {
    Optional<Instructor> findByUserEID(String userEID);
    Optional<Instructor> findById(Integer id);
    long count();
    List<Instructor> findAll();
}
