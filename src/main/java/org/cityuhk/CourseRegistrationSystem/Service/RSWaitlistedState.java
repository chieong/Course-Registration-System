package org.cityuhk.CourseRegistrationSystem.Service;

public class RSWaitlistedState implements RegistrationState {
    @Override
    public void confirm(RegistrationRecord record) {
        // Only called by the system when a spot opens
        record.getSection().incrementEnrollment();
        record.setState(new RSConfirmedState());
    }

    @Override
    public void drop(RegistrationRecord record) {
        // Simply remove from queue and cancel
        record.getSection().removeFromWaitlist(record.getStudent());
        record.setState(new RSCancelledState());
    }

    @Override
    public void waitlist(RegistrationRecord record) {
        throw new IllegalStateException("Already waitlisted.");
    }

    @Override
    public String getStatusName() { return "WAITLISTED"; }
}
