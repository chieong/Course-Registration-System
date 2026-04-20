package org.cityuhk.CourseRegistrationSystem.Exception;

/**
 * Base exception for user validation errors.
 */
public class UserValidationException extends UserManagementException {
    private final String fieldName;

    public UserValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
