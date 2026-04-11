package org.cityuhk.CourseRegistrationSystem.Service;

public class RSConfirmedState implements RegistrationState {
    @Override
    public void confirm(RegistrationRecord record) {
        throw new IllegalStateException("Already confirmed.");
    }

    @Override
    public void drop(RegistrationRecord record) {
        record.getSection().decrementEnrollment();
        record.setState(new RSCancelledState());
    }

    @Override
    public void waitlist(RegistrationRecord record) {
        throw new IllegalStateException("Cannot waitlist a confirmed student.");
    }

    @Override
    public String getStatusName() { 
        return "CONFIRMED"; 
    }
}