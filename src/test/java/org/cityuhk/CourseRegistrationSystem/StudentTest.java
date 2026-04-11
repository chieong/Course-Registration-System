package org.cityuhk.CourseRegistrationSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cityuhk.CourseRegistrationSystem.Service.Student;
import org.junit.jupiter.api.Test;

public class StudentTest {
    
	@Test
	void validateSemesterCreditCountTest() {
		Student stu=new Student(null,null,0,5,20,null,0,null,0);
		assertEquals(true,stu.validateSemesterCreditCount(10));
	}
	
	@Test
	void FailedvalidateSemesterCreditCountTest() {
		Student stu=new Student(null,null,0,5,20,null,0,null,0);
		assertEquals(false,stu.validateSemesterCreditCount(3));
	}
	
	@Test
	void GetDepartment(){
		Student stu=new Student(null,null,0,5,20,null,0,"Clash Royale",0);
		assertEquals("Clash Royale",stu.getDepartment());
	}
}
