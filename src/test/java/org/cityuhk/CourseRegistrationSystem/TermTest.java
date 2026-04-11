package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.Term;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class TermTest {

	@Test
	void termCanBeInstantiatedTest() {
		Term term = new Term(2026, "Semester A");
		assertNotNull(term);
	}
}
