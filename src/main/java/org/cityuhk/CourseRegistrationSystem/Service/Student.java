package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.HashSet;
import java.util.Set;


public class Student extends User implements IAcademic, IStudent {

	private int studentId;
	private int minSemesterCredit;
	private int maxSemesterCredit; 
	private String major;
	private int cohort;
	private String department;
	private int maxDegreeCredit; // this is the total credit for each student for their degree 

	public Student(StudentBuilder builder) {
		super(builder);
		this.studentId = builder.studentId;
		this.minSemesterCredit = builder.minSemesterCredit;
		this.maxSemesterCredit = builder.maxSemesterCredit;
		this.major = builder.major;
		this.cohort = builder.cohort;
		this.department = builder.department;
		this.maxDegreeCredit = builder.maxDegreeCredit;
    }

	public static class StudentBuilder extends User.Builder<StudentBuilder> {
		private int studentId;
		private int minSemesterCredit;
		private int maxSemesterCredit; 
		private String major;
		private int cohort;
		private String department;
		private int maxDegreeCredit;

		public StudentBuilder withStudentId(int studentId) {
			this.studentId = studentId;
			return self();
		}

		public StudentBuilder withMinSemesterCredit(int minSemesterCredit) {
			this.minSemesterCredit = minSemesterCredit;
			return self();
		}

		public StudentBuilder withMaxSemesterCredit(int maxSemesterCredit) {
			this.maxSemesterCredit = maxSemesterCredit;
			return self();
		}

		public StudentBuilder withMajor(String major) {
			this.major = major;
			return self();
		}

		public StudentBuilder withCohort(int cohort) {
			this.cohort = cohort;
			return self();
		}

		public StudentBuilder withDepartment(String department) {
			this.department = department;
			return self();
		}

		public StudentBuilder withMaxDegreeCredit(int maxDegreeCredit) {
			this.maxDegreeCredit = maxDegreeCredit;
			return self();
		}

		@Override
		protected StudentBuilder self() {
			return this;
		}

		@Override
		public Student build() {
			return new Student(this);
		}
	}


	// Stores the student's registered classes (their timetable), not implemented yet
	private Set<Course> Allcourse = new HashSet<>();
	// should be deleted later

	@Override
	public boolean validateSemesterCreditCount(int newCredit) {
		
        return (newCredit >=minSemesterCredit && newCredit <= maxSemesterCredit);
	}

	@Override
	public String getDepartment() {
		return department;
	}

	
	//getter
	//add when needed

}