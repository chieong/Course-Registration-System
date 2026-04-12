package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;

import org.cityuhk.CourseRegistrationSystem.Model.AcademicRecord;

public interface IAcademicRecordRepository {

	/**
	 * 
	 * @param studentId
	 */
	ArrayList<AcademicRecord> getStudentRecord(int studentId);

}