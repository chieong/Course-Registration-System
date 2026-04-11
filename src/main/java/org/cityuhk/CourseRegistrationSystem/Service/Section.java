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
	private ArrayList<IStudent> enrolledStudent;
	private ArrayList<IStudent> waitlistedStudent;
	
	public void enrollStudent(IStudent Student) {
		if (hasSpace()) {
			enrolledStudent.add(Student);
			incrementEnrollment();
		} else {
			//TODO - throw exception or add to waitlist
			throw new UnsupportedOperationException("Section is full.");
		}
	}

	public void dropStudent(IStudent Student) {
		waitlistedStudent.remove(Student);
		enrolledStudent.remove(Student);
	}

	public void waitlistStudent(IStudent Student) {
		if (waitlistCount < waitlistCapacity) {
			waitlistedStudent.add(Student);
			waitlistCount++;
		} else {
			//TODO - throw exception
			throw new UnsupportedOperationException("Waitlist is full.");
		}
	}

	//getter
	public int getSectionID() {
		return sectionId;
	}

	public boolean hasSpace() {
		return enrolledCount < capacity;
	}

    public void incrementEnrollment() {
		enrolledCount++;
    }

    public void decrementEnrollment() {
        enrolledCount--;
    }

    public void notifyWaitlist() {

    }

    public void removeFromWaitlist(Student student) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeFromWaitlist'");
    }

}