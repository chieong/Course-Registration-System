package org.cityuhk.CourseRegistrationSystem.Exception;

/**
 * Base exception for all user management domain errors.
 */
public class UserManagementException extends RuntimeException {
    public UserManagementException(String message) {
        super(message);
    }

    public UserManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
