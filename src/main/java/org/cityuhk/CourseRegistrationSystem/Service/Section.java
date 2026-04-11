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

	
	public Section(int sectionId, int capacity, int enrolledCount, int waitlistCapacity, String type, String venue,
			Course course, int waitlistCount, ArrayList<IStudent> enRolledStudent,
			ArrayList<IStudent> waitlistedStudent) {
		this.sectionId = sectionId;
		this.capacity = capacity;
		this.enrolledCount = enrolledCount;
		this.waitlistCapacity = waitlistCapacity;
		this.type = type;
		this.venue = venue;
		this.course = course;
		this.waitlistCount = waitlistCount;
		this.enRolledStudent = enRolledStudent;
		this.waitlistedStudent = waitlistedStudent;
	}



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

	// getter
	public int getSectionID() {
		return sectionId;
	}

}