package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class InstructorTest {

	private Instructor createInstructor() {
		Instructor.InstructorBuilder builder = new Instructor.InstructorBuilder()
				.withUserEID("i001")
				.withName("Alice")
				.withStaffId(87)
				.withDepartment("Clash Royale");
		return new Instructor(builder);
	}
    
	@Test
	void getStaffIdTest() {
		Instructor i = createInstructor();
		assertThrows(UnsupportedOperationException.class, i::getStaffId);
	}
	
	@Test
	void getDepartment() {
		Instructor i = createInstructor();
		assertEquals("Clash Royale",i.getDepartment());
	}

	@Test
	void getUserEIDTest() {
		Instructor i = createInstructor();
		assertEquals("i001", i.getUserEID());
	}

	@Test
	void getUserNameTest() {
		Instructor i = createInstructor();
		assertEquals("Alice", i.getUserName());
	}

	@Test
	void getTimeTable() {
		Instructor i = createInstructor();
		assertThrows(UnsupportedOperationException.class, i::getTimeTable);
	}

	@Test
	void instructorBuilderBuildReturnsInstructorTest() {
		User builtUser = new Instructor.InstructorBuilder()
				.withUserEID("i002")
				.withName("Bob")
				.withStaffId(88)
				.withDepartment("CS")
				.build();

		Instructor instructor = assertInstanceOf(Instructor.class, builtUser);
		assertEquals("i002", instructor.getUserEID());
		assertEquals("Bob", instructor.getUserName());
		assertEquals("CS", instructor.getDepartment());
	}
	
}
