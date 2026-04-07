package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
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

	@Override
	public boolean validateSemesterCreditCount(int newCredit) {
		int currentCredits = 0;
        for (Course r : Allcourse) {
            currentCredits += r.getCredits(); // each record has a credit value
        }
        // e.g. current=12, new=4, max=18 → 12+4=16 ≤ 18 → true ✅
        // e.g. current=16, new=4, max=18 → 16+4=20 > 18  → false ❌
        return (currentCredits + newCredit) <= maxSemesterCredit;
	}

	@Override
	public String getDepartment() {
		return department;

	}

	@Override
	public ArrayList<Section> getTimeTable() {

		return timeTable;
	}

	//getter
	//add when needed

}