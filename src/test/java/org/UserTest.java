package org;

import org.cityuhk.CourseRegistrationSystem.Service.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class UserTest {

	private static class TestUser extends User {
		TestUser(String userEID, String name) {
			super(userEID, name);
		}
	}

	@Test
	void getUserEIDTest() {
		TestUser user = new TestUser("e123", "Alice");
		assertEquals("e123", user.getUserEID());
	}

	@Test
	void getUserNameTest() {
		TestUser user = new TestUser("e123", "Alice");
		assertEquals("Alice", user.getUserName());
	}


}
