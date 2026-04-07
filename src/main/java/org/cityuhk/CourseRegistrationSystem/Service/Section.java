package org.cityuhk.CourseRegistrationSystem.Service;
import java.util.ArrayList;

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

	//getter
	public int getSectionID() {
		return sectionId;
	}

	public int getEnrolledCount() {
		return enrolledCount;
	}

	public int getCapacity() {
		return capacity;
	}

	public void IncremenEnrolledCount() {
		enrolledCount++;
	}

	public Course getCourse() {
		return course;
	}

}