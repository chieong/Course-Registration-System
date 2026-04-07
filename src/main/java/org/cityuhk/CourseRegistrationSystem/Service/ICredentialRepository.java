package org.cityuhk.CourseRegistrationSystem.Service;
public interface ICredentialRepository {

	boolean validateCredential(String userEID, String userPassword);
	
}