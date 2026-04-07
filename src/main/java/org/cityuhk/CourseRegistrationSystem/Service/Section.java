package org.cityuhk.CourseRegistrationSystem.Service;
import java.util.ArrayList;
import java.time.LocalDate;

public class Section {
	// A section under a course like lab, lecture..etc
	
	private int sectionId;
	private int capacity;
	private int enrolledCount;
	private int waitlistCapacity;
	private String type;
	private String venue;
	private Course course;
	private int waitlistCount;
	private ArrayList<IStudent> enRolledStudent;
	private ArrayList<IStudent> waitlistedStudent;
	private 
	

	/**
	 * 
	 * @param Student
	 */
	public void enrollStudent(IStudent Student) {
		// TODO - implement Section.enrollStudent
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param Student
	 */
	public void waitlistStudent(IStudent Student) {
		// TODO - implement Section.waitlistStudent
		throw new UnsupportedOperationException();
	}

	//getter
	public int getSectionID() {
		return sectionId;
	}

}