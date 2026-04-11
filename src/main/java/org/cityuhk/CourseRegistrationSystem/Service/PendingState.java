package org.cityuhk.CourseRegistrationSystem.Service;

public class PendingState implements RegistrationState {
    @Override
    public void confirm(RegistrationRecord record) {
        // Business Logic: Check if section is full before moving to Confirmed
        if (record.getSection().hasSpace()) {
            record.getSection().incrementEnrollment();
            record.setState(new ConfirmedState());
            System.out.println("Registration confirmed.");
        } else {
            this.waitlist(record);
        }
    }

    @Override
    public void drop(RegistrationRecord record) {
        record.setState(new CancelledState());
    }

    @Override
    public void waitlist(RegistrationRecord record) {
        record.setState(new WaitlistedState());
    }

    @Override
    public String getStatusName() { return "PENDING"; }
}