package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class UserTest {

	private static class TestUser extends User {
		TestUser(TestUserBuilder builder) {
			super(builder);
		}

		static class TestUserBuilder extends User.Builder<TestUserBuilder> {
			@Override
			protected TestUserBuilder self() {
				return this;
			}

			@Override
			public TestUser build() {
				return new TestUser(this);
			}
		}
	}

	private TestUser createUser(String userEID, String name) {
		return new TestUser.TestUserBuilder()
				.withUserEID(userEID)
				.withName(name)
				.build();
	}

	@Test
	void getUserEIDTest() {
		TestUser user = createUser("e123", "Alice");
		assertEquals("e123", user.getUserEID());
	}

	@Test
	void getUserNameTest() {
		TestUser user = createUser("e123", "Alice");
		assertEquals("Alice", user.getUserName());
	}


}
