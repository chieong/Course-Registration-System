package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.InstructorUserRequest;

public interface InstructorUserManagementOperations {
    List<Instructor> listInstructors();
    Instructor createInstructor(InstructorUserRequest request);
    Instructor modifyInstructor(Integer staffId, InstructorUserRequest request);
    void removeInstructor(Integer staffId);
}
