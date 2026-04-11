package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.LocalDatabaseControl;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class LocalDatabaseControlTest {

	private LocalDatabaseControl createDb() {
		return new LocalDatabaseControl();
	}

	@Test
	void getCourseByIdTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.getCourseById("CS101"));
	}

	@Test
	void getAllCourseTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, db::getAllCourse);
	}

	@Test
	void createCourseTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.createCourse(null));
	}

	@Test
	void updateCourseTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.updateCourse("CS101", null));
	}

	@Test
	void removerCourseTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.removerCourse("CS101"));
	}

	@Test
	void addRegistrationRecordTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.addRegistrationRecord(null));
	}

	@Test
	void getStudentRegistrationRecordByStudentTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.getStudentRegistrationRecordByStudent(1001));
	}

	@Test
	void pollNextFromWaitlistTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.pollNextFromWaitlist(1));
	}

	@Test
	void addPlanRecordTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.addPlanRecord(null));
	}

	@Test
	void getPlanByIdTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.getPlanById(1));
	}

	@Test
	void addRegistrationPeriodTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.addRegistrationPeriod(null));
	}

	@Test
	void removeRegistrationPeriodTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.removeRegistrationPeriod(1));
	}

	@Test
	void getAllRegistrationPeriodTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, db::getAllRegistrationPeriod);
	}

	@Test
	void validateCredentialTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.validateCredential("user", "pass"));
	}

	@Test
	void getUserTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.getUser("u001"));
	}

	@Test
	void createUserTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.createUser(null));
	}

	@Test
	void modifyUserTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.modifyUser("u001", null));
	}

	@Test
	void deleteUserTest() {
		LocalDatabaseControl db = createDb();
		assertThrows(UnsupportedOperationException.class, () -> db.deleteUser("u001"));
	}
}
