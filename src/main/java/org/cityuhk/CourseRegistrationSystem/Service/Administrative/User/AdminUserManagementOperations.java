package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;

public interface AdminUserManagementOperations {
    List<Admin> listUsers();
    Admin createUser(AdminUserRequest request);
    Admin modifyUser(Integer staffId, AdminUserRequest request);
    void removeUser(Integer staffId);
}
