package org.cityuhk.CourseRegistrationSystem.Service;

public class RSCancelledState implements RegistrationState {
    
    @Override
    public void confirm(RegistrationRecord record) {
        throw new IllegalStateException("Cannot confirm a cancelled registration.");
    }

    @Override
    public void drop(RegistrationRecord record) {
        // Already dropped/cancelled.
        throw new IllegalStateException("Cancelled records cannot be dropped again.");
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