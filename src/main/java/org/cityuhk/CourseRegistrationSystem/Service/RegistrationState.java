package org.cityuhk.CourseRegistrationSystem.Service;

public interface RegistrationState {
    void confirm(RegistrationRecord record);
    void drop(RegistrationRecord record);
    void waitlist(RegistrationRecord record);
    String getStatusName();
}
