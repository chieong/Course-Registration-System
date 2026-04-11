package org.cityuhk.CourseRegistrationSystem;

import java.util.ArrayList;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationPlanManager;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class RegistrationPlanManagerTest {

	@Test
	void createPlanThrowsWhenNotImplementedTest() {
		RegistrationPlanManager manager = new RegistrationPlanManager();
		assertThrows(UnsupportedOperationException.class, () -> manager.createPlan(new ArrayList<>()));
	}

	@Test
	void submitPlanThrowsWhenNotImplementedTest() {
		RegistrationPlanManager manager = new RegistrationPlanManager();
		assertThrows(UnsupportedOperationException.class, () -> manager.submitPlan(1));
	}

	@Test
	void viewPlansByStudentThrowsWhenNotImplementedTest() {
		RegistrationPlanManager manager = new RegistrationPlanManager();
		assertThrows(UnsupportedOperationException.class, () -> manager.viewPlansByStudent(1001));
	}
}
