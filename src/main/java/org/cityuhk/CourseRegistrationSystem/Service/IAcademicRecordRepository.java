package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
public interface IAcademicRecordRepository {

	/**
	 * 
	 * @param studentId
	 */
	ArrayList<AcademicRecord> getStudentRecord(int studentId);

}