package org.cityuhk.CourseRegistrationSystem.Service;
public class Admin extends User implements IStaff {

	private int staffId;

	public Admin(String userEID, String name) {
		super(userEID, name);
	}

	public int getStaffId() {
		// TODO - implement Admin.getStaffId
		throw new UnsupportedOperationException();
	}

}