package org.cityuhk.CourseRegistrationSystem.Exception;

/**
 * Thrown when user name is invalid (blank, null, etc).
 */
public class InvalidNameException extends UserValidationException {
    public InvalidNameException() {
        super("name", "Name is required");
    }

    public InvalidNameException(String reason) {
        super("name", "Invalid name: " + reason);
    }
}
