package org.cityuhk.CourseRegistrationSystem.Exception;

/**
 * Thrown when user password is invalid (blank, null, etc).
 */
public class InvalidPasswordException extends UserValidationException {
    public InvalidPasswordException() {
        super("password", "Password is required");
    }

    public InvalidPasswordException(String reason) {
        super("password", "Invalid password: " + reason);
    }
}
