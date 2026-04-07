package org.cityuhk.CourseRegistrationSystem.Service;
public class RegistrationManager {

	private IRegistrationRecordRepository registrationRecordRepo;
	public RegistrationManager(IRegistrationRecordRepository repo) {
    this.registrationRecordRepo = repo;  
}

	/**
	 * 
	 * @param student
	 * @param section
	 */
	public void registerSection(IStudent student, Section section) {
		// ✅ Check 1: Is there space in the section?
		if (section.getEnrolledCount() >= section.getCapacity()) {
        throw new RuntimeException("Section is full!");	
    }

	 if (!student.validateSemesterCreditCount(section.getCourse().getCredits())) {
        throw new RuntimeException("Student exceeded max credits!");
    }

    // ✅ Make a "receipt" of this enrollment
   	 RegistrationRecord record = new RegistrationRecord(student, section);

    // ✅ Save it to the database via the repo (filing cabinet)
    registrationRecordRepo.addRegistrationRecord(record);

    // ✅ Update the count in the section
    section.IncremenEnrolledCount();
	}

	/**
	 * 
	 * @param student
	 * @param section
	 */
	public void dropSection(IStudent student, Section section) {

	}

	/**
	 * 
	 * @param student
	 * @param section
	 */
	public void joinSectionWaitlist(IStudent student, Section section) {

	}

	/**
	 * 
	 * @param user
	 */
	public void getTimeTable(IAcademic user) {
		// TODO - implement RegistrationManager.getTimeTable
		throw new UnsupportedOperationException();
	}

}