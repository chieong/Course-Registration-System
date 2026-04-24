package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminStudentRequest;

public interface StudentUserManagementOperations {
    List<Student> listStudents();
    Student createStudent(AdminStudentRequest request);
    Student modifyStudent(AdminStudentRequest request);
    void removeStudent(String userEID);
}
