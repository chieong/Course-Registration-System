package org.cityuhk.CourseRegistrationSystem.Service;
public class RegistrationRecord {

	private Student student;
	private int timestamp;
	private Section section;
	private IStudent istudent;
	private RegistrationOpType operationType;

	private RegistrationRecord(Student student, Section section) {
		this.student = student;
		this.section = section;
	}

}