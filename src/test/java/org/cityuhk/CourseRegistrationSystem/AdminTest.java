package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.Admin;
import org.cityuhk.CourseRegistrationSystem.Service.Admin.AdminBuilder;
import org.cityuhk.CourseRegistrationSystem.Service.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;

public class AdminTest {

    private Admin createAdmin() {
		AdminBuilder builder = new AdminBuilder()
                .withUserEID("a001")
                .withName("Alice")
		.withStaffId(87);
		return new Admin(builder);
    }

	@Test
	void getUserEIDTest() {
		Admin admin = createAdmin();
		assertEquals("a001", admin.getUserEID());
	}

	@Test
	void getUserNameTest() {
		Admin admin = createAdmin();
		assertEquals("Alice", admin.getUserName());
	}

	@Test
	void getStaffIdTest() {
		Admin admin = createAdmin();
		assertEquals(87, admin.getStaffId());
	}

	@Test
	void adminBuilderBuildReturnsAdminTest() {
		User builtUser = new AdminBuilder()
				.withUserEID("a002")
				.withName("Bob")
				.withStaffId(99)
				.build();

		Admin admin = assertInstanceOf(Admin.class, builtUser);
		assertEquals("a002", admin.getUserEID());
		assertEquals("Bob", admin.getUserName());
		assertEquals(99, admin.getStaffId());
	}
}
