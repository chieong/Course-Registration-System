package org;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationManager;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class RegistrationManagerTest {

	@Test
	void registerSectionDoesNotThrowTest() {
		RegistrationManager manager = new RegistrationManager();
		assertDoesNotThrow(() -> manager.registerSection(null, null));
	}

	@Test
	void dropSectionDoesNotThrowTest() {
		RegistrationManager manager = new RegistrationManager();
		assertDoesNotThrow(() -> manager.dropSection(null, null));
	}

	@Test
	void joinSectionWaitlistDoesNotThrowTest() {
		RegistrationManager manager = new RegistrationManager();
		assertDoesNotThrow(() -> manager.joinSectionWaitlist(null, null));
	}

	@Test
	void getTimeTableThrowsWhenNotImplementedTest() {
		RegistrationManager manager = new RegistrationManager();
		assertThrows(UnsupportedOperationException.class, () -> manager.getTimeTable(null));
	}
}
