package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;

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