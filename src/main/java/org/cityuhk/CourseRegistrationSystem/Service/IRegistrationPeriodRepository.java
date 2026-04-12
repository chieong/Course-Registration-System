package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;

public interface IRegistrationPeriodRepository {

	/**
	 * 
	 * @param rp
	 */
	int addRegistrationPeriod(RegistrationPeriod rp);

	/**
	 * 
	 * @param registrationPeriodID
	 */
	void removeRegistrationPeriod(int registrationPeriodID);

	ArrayList<RegistrationPeriod> getAllRegistrationPeriod();

}