package org.cityuhk.CourseRegistrationSystem.Service;

public class RegistrationRecord {

    private Student student;
    private Section section;
    private long timestamp;
    private RegistrationOpType operation;

    public RegistrationRecord(
            Student student, Section section, long timestamp, RegistrationOpType operation) {
        this.student = student;
        this.section = section;
        this.timestamp = timestamp;
        this.operation = operation;
    }
}

