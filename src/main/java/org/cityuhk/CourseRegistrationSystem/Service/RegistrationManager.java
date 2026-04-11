package org.cityuhk.CourseRegistrationSystem.Service;

public class RegistrationManager {

    private final IRegistrationRecordRepository registrationRecordRepository;

    public RegistrationManager(IRegistrationRecordRepository registrationRecordRepository) {
        this.registrationRecordRepository = registrationRecordRepository;
    }

    /**
     * @param student
     * @param section
     */
    public void addSection(Student student, Section section) {
        registrationRecordRepository.addRegistrationRecord(new RegistrationRecord(student, section, System.currentTimeMillis(), RegistrationOpType.ADD));
    }

    /**
     * @param student
     * @param section
     */
    public void dropSection(IStudent student, Section section) {}

    /**
     * @param student
     * @param section
     */
    public void joinSectionWaitlist(IStudent student, Section section) {}

    /**
     * @param user
     */
    public void getTimeTable(IAcademic user) {
        // TODO - implement RegistrationManager.getTimeTable
        throw new UnsupportedOperationException();
    }
}

