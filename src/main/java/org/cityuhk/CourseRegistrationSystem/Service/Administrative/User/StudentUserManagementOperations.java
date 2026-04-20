package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.StudentUserRequest;

public interface StudentUserManagementOperations {
    List<Student> listStudents();
    Student createStudent(StudentUserRequest request);
    Student modifyStudent(Integer studentId, StudentUserRequest request);
    void removeStudent(Integer studentId);
}
