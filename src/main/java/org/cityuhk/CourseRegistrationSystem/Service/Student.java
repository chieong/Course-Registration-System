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

	public Student(String userEID, String name, int studentId,
                   int minSemesterCredit, int maxSemesterCredit, String major,
                   int cohort, String department, int maxDegreeCredit) {
        super(userEID, name);
        this.studentId = studentId;
        this.minSemesterCredit = minSemesterCredit;
        this.maxSemesterCredit = maxSemesterCredit;
        this.major = major;
        this.cohort = cohort;
        this.department = department;
        this.maxDegreeCredit = maxDegreeCredit;
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