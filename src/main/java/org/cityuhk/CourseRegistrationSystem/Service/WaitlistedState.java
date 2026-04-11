package org.cityuhk.CourseRegistrationSystem.Service;

public class WaitlistedState implements RegistrationState {
    @Override
    public void confirm(RegistrationRecord record) {
        // Only called by the system when a spot opens
        record.getSection().incrementEnrollment();
        record.setState(new ConfirmedState());
    }

    @Override
    public void drop(RegistrationRecord record) {
        // Simply remove from queue and cancel
        record.getSection().removeFromWaitlist(record.getStudent());
        record.setState(new CancelledState());
    }

    @Override
    public void waitlist(RegistrationRecord record) {
        // Already waitlisted
    }

    @Override
    public String getStatusName() { return "WAITLISTED"; }
}
