package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
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
	void pollNextFromWaitlist(int sectionId);

}