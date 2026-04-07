package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
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