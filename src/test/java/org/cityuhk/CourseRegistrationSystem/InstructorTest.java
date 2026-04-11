package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.Instructor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class InstructorTest {
    
	@Test
	void getStaffIdTest() {
		Instructor i=new Instructor("nigga","nigga1",87,"Clash Royale");
		assertEquals(87,i.getStaffId());
	}
	
	@Test
	void getDepartment() {
		Instructor i=new Instructor("nigga1","nigga2",87,"Clash Royale"); 
		assertEquals("Clash Royale",i.getDepartment());
	}

	@Test
	void getTimeTable() {
		Instructor i=new Instructor("nigga2","nigga3",87,"Clash Royale"); 
		assertThrows(UnsupportedOperationException.class, i::getTimeTable);
	}
	
}
