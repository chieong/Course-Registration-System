package org.cityuhk.CourseRegistrationSystem.Service;
public abstract class User {

	private String UserEID;
	private String name;

	public User(String userEID, String name) {
		this.UserEID = userEID;
		this.name = name;
	}

	public String getUserEID() {
		return UserEID;
	}

	public String getUserName() {
		return name;
	}

}