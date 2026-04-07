package org.cityuhk.CourseRegistrationSystem.Service;
public interface IRegistrationRecordRepository {

	/**
	 * 
	 * @param record
	 */
	void addRegistrationRecord(RegistrationRecord record);

	/**
	 * 
	 * @param studentId
	 */
	ArrayList<RegistrationRecord> getStudentRegistrationRecordByStudent(int studentId);

	/**
	 * 
	 * @param sectionId
	 */
	void pollNextFromWaitlist(id sectionId);

}