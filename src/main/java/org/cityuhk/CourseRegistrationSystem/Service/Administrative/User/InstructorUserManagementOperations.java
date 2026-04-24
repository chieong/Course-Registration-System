package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminInstructorRequest;

public interface InstructorUserManagementOperations {
    List<Instructor> listInstructors();
    Instructor createInstructor(AdminInstructorRequest request);
    Instructor modifyInstructor(AdminInstructorRequest request);
    void removeInstructor(String userEID);
}
