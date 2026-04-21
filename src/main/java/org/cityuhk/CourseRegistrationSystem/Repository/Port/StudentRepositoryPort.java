package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import java.util.Optional;

public interface StudentRepositoryPort {
    Optional<Student> findByUserEID(String userEID);
    Optional<Student> findById(Integer id);
}
