package org.cityuhk.CourseRegistrationSystem.Exception;

/**
 * Thrown when attempting to create or modify a user with an EID that already exists in any role.
 */
public class UserEidAlreadyExistsException extends UserManagementException {
    private final String eid;

    public UserEidAlreadyExistsException(String eid) {
        super("User EID already exists: " + eid);
        this.eid = eid;
    }

    public String getEid() {
        return eid;
    }
}
