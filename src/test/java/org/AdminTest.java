package org;

import org.cityuhk.CourseRegistrationSystem.Service.Admin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class AdminTest {

	@Test
	void getUserEIDTest() {
		Admin admin = new Admin("a001", "Alice");
		assertEquals("a001", admin.getUserEID());
	}

	@Test
	void getUserNameTest() {
		Admin admin = new Admin("a001", "Alice");
		assertEquals("Alice", admin.getUserName());
	}

	@Test
	void getStaffIdThrowsWhenNotImplemented() {
		Admin admin = new Admin("a001", "Alice");
		assertThrows(UnsupportedOperationException.class, admin::getStaffId);
	}
}
