package org.cityuhk.CourseRegistrationSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cityuhk.CourseRegistrationSystem.Service.CourseRegistrationSystem;
import org.cityuhk.CourseRegistrationSystem.Service.ICredentialRepository;
import org.cityuhk.CourseRegistrationSystem.Service.SessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


class CourseRegistrationAppTests {
	private CourseRegistrationSystem crs=CourseRegistrationSystem.getInstance();
	
	@Test
	public void testnewSession() {
		
		class StubcreateNewSession implements ICredentialRepository{
			public boolean validateCredential(String userEID, String userPassword) {
				return true;
			}
		}
		String result = crs.newSession("Test","1234");
		assertEquals("Test",crs.getEIDBySession(result));
	}

}
