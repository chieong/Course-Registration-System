package org.cityuhk.CourseRegistrationSystem.Exception;

/**
 * Thrown when user EID is invalid (blank, null, etc).
 */
public class InvalidUserEIDException extends UserValidationException {
    public InvalidUserEIDException() {
        super("userEID", "User EID is required");
    }

    public InvalidUserEIDException(String reason) {
        super("userEID", "Invalid User EID: " + reason);
    }
}
