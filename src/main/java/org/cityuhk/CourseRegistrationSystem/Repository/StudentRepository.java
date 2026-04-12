package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
