package org.cityuhk.CourseRegistrationSystem.Service;
public class RegistrationRecord {

	private IStudent student;
	private int timestamp;
	private Section section;
	private RegistrationOpType operationType;

	public RegistrationRecord(IStudent student, Section section) {
		this.student = student;
		this.section = section;
	}

}