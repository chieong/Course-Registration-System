package org.cityuhk.CourseRegistrationSystem.Service;

public class RSCancelledState implements RegistrationState {
    
    @Override
    public void confirm(RegistrationRecord record) {
        //do nothing, cannot confirm a cancelled record
    }

    @Override
    public void drop(RegistrationRecord record) {
        //do nothing, cannot drop a cancelled record
    }

    @Override
    public void waitlist(RegistrationRecord record) {
        //do nothing, cannot waitlist a cancelled record
    }

    @Override
    public String getStatusName() {
        return "CANCELLED";
    }
}