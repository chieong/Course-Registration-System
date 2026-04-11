package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
public class Instructor extends User implements IStaff, IAcademic {

	private int staffId;
	private String department;

	public Instructor(String userEID, String name,int id,String department) {
		super(userEID, name);
		this.staffId=id;
		this.department=department;
	}

	public int getStaffId() {
		return staffId;
	}

	public String getDepartment() {
		return this.department;
	}

	public ArrayList<RegistrationRecord> getTimeTable() {
		// TODO - implement Instructor.getTimeTable
		throw new UnsupportedOperationException();
	}

}