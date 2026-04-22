package org.cityuhk.CourseRegistrationSystem.Service.Administrative;

public class RegistrationPeriodOverlapException extends RuntimeException {

    public RegistrationPeriodOverlapException(String message) {
        super(message);
    }
}
