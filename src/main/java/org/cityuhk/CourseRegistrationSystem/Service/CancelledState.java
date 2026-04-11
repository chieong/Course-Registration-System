package org.cityuhk.CourseRegistrationSystem.Service;

public class CancelledState implements RegistrationState {
    
    @Override
    public void confirm(RegistrationRecord record) {
        throw new IllegalStateException("Cannot confirm a cancelled registration.");
    }

    @Override
    public void drop(RegistrationRecord record) {
        // Already dropped/cancelled.
        System.out.println("Registration is already in cancelled state.");
    }

    @Override
    public void waitlist(RegistrationRecord record) {
        throw new IllegalStateException("Cancelled records cannot be moved to waitlist.");
    }

    @Override
    public String getStatusName() {
        return "CANCELLED";
    }
}