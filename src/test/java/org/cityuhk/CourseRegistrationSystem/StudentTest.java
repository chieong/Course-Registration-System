package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class StudentTest {

	private Student createStudent(String department) {
		return new Student.StudentBuilder()
				.withUserEID("s001")
				.withName("Bob")
				.withStudentId(1)
				.withMinSemesterCredit(5)
				.withMaxSemesterCredit(20)
				.withMajor("CS")
				.withCohort(2024)
				.withDepartment(department)
				.withMaxDegreeCredit(120)
				.build();
	}
    
	@Test
	void validateSemesterCreditCountTest() {
		Student stu = createStudent(null);
		assertEquals(true,stu.validateSemesterCreditCount(10));
	}
	
	@Test
	void FailedvalidateSemesterCreditCountTestByExceedingMaxCredit() {
		Student stu = createStudent(null);
		assertEquals(false,stu.validateSemesterCreditCount(23));
	}

	@Test
	void FailedvalidateSemesterCreditCountTestByExceedingMinCredit() {
		Student stu = createStudent(null);
		assertEquals(false,stu.validateSemesterCreditCount(2));
	}
	
	@Test
	void GetDepartment(){
		Student stu = createStudent("Clash Royale");
		assertEquals("Clash Royale",stu.getDepartment());
	}

	

}
