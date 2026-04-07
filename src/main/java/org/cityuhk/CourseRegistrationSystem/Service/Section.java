package org.cityuhk.CourseRegistrationSystem.Service;
import java.util.ArrayList;

public class Section {

	
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

}