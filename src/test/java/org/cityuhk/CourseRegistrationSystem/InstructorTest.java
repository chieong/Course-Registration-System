package org.cityuhk.CourseRegistrationSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cityuhk.CourseRegistrationSystem.Service.Instructor;
import org.junit.jupiter.api.Test;

public class InstructorTest {
    
	@Test
	void getStaffIdTest() {
		Instructor i=new Instructor("Phelim8787","Phelim",87,"Clash Royale");
		assertEquals(87,i.getStaffId());
	}
	
	@Test
	void getDepartment() {
		Instructor i=new Instructor("Phelim8787","Phelim",87,"Clash Royale");
		assertEquals("Clash Royale",i.getDepartment());
	}
}
