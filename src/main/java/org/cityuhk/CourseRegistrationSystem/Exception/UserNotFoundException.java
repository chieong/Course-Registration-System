package org.cityuhk.CourseRegistrationSystem.Exception;

/**
 * Thrown when attempting to modify or remove a user that does not exist.
 */
public class UserNotFoundException extends UserManagementException {
    private final String userRole;
    private final Integer userId;

    public UserNotFoundException(String userRole, Integer userId) {
        super(userRole + " user not found: id=" + userId);
        this.userRole = userRole;
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public Integer getUserId() {
        return userId;
    }
}
