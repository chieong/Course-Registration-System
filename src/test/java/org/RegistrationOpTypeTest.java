package org;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationOpType;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class RegistrationOpTypeTest {

	@Test
	void valueOfTest() {
		assertEquals(RegistrationOpType.ADD, RegistrationOpType.valueOf("ADD"));
		assertEquals(RegistrationOpType.DROP, RegistrationOpType.valueOf("DROP"));
		assertEquals(RegistrationOpType.WAITLIST, RegistrationOpType.valueOf("WAITLIST"));
	}

	@Test
	void valuesOrderTest() {
		RegistrationOpType[] expected = {
			RegistrationOpType.ADD,
			RegistrationOpType.DROP,
			RegistrationOpType.WAITLIST
		};
		assertArrayEquals(expected, RegistrationOpType.values());
	}

	@Test
	void nameTest() {
		assertEquals("ADD", RegistrationOpType.ADD.name());
		assertEquals("DROP", RegistrationOpType.DROP.name());
		assertEquals("WAITLIST", RegistrationOpType.WAITLIST.name());
	}
}
