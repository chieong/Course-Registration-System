package org.cityuhk.CourseRegistrationSystem.Exception;

/**
 * Thrown when attempting to modify or remove a user that does not exist.
 */
public class UserNotFoundException extends UserManagementException {
    private final String userRole;
    private final String userEID;

    public UserNotFoundException(String userRole, String userEID) {
        super(userRole + " user not found: id=" + userEID);
        this.userRole = userRole;
        this.userEID = userEID;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getUserEID() {
        return userEID;
    }
}
