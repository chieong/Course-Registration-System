package org.cityuhk.CourseRegistrationSystem.Service;
public interface IAcademicRecordRepository {

	/**
	 * 
	 * @param studentId
	 */
	ArrayList<AcademicRecord> getStudentRecord(id studentId);

}