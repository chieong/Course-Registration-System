package org.cityuhk.CourseRegistrationSystem.Service;

public class ConfirmedState implements RegistrationState {
    @Override
    public void confirm(RegistrationRecord record) {
        // TODO : bro what why would you confirm a confirmed record
    }

    @Override
    public void drop(RegistrationRecord record) {
        // Business Logic: Free up a seat in the section
        record.getSection().decrementEnrollment();
        record.setState(new CancelledState());
        
        // Trigger logic to notify the next person on the waitlist
        record.getSection().notifyWaitlist();
    }

    @Override
    public void waitlist(RegistrationRecord record) {
        throw new IllegalStateException("Cannot waitlist a confirmed student.");
    }

    @Override
    public String getStatusName() { return "CONFIRMED"; }
}