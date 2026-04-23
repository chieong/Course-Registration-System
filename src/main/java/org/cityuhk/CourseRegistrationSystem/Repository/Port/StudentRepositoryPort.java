package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepositoryPort {
    Optional<Student> findByUserEID(String userEID);
    Optional<Student> findById(Integer id);
    Student save(Student student);
    void deleteById(Integer id);
    List<Student> findAll();
    boolean existsById(Integer id);
    long count();
}
