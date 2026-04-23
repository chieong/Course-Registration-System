package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

public class TimetableValidationException extends Exception {
    
    public TimetableValidationException(String message) {
        super(message);
    }
    
    public TimetableValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
