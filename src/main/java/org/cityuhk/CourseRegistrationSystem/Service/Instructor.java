package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
public class Instructor extends User implements IStaff, IAcademic {

	private int staffId;
	private String department;

	public void getStaffId() {
		// TODO - implement Instructor.getStaffId
		throw new UnsupportedOperationException();
	}

	public String getDepartment() {
		return this.department;
	}

	public ArrayList<RegistrationRecord> getTimeTable() {
		// TODO - implement Instructor.getTimeTable
		throw new UnsupportedOperationException();
	}

}