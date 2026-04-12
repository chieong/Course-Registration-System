package org.cityuhk.CourseRegistrationSystem.Service;

import org.cityuhk.CourseRegistrationSystem.Model.User;

public interface IUserRepository {

	/**
	 * 
	 * @param userEID
	 */
	User getUser(String userEID);

	/**
	 * 
	 * @param user
	 */
	void createUser(User user);

	/**
	 * 
	 * @param userEID
	 * @param user
	 */
	void modifyUser(String userEID, User user);

	/**
	 * 
	 * @param userEID
	 */
	void deleteUser(String userEID);

}