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

	}

	public void dropStudent(IStudent Student) {
		waitlistedStudent.remove(Student);
		enrolledStudent.remove(Student);
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

	public boolean hasSpace() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'hasSpace'");
	}

    public void incrementEnrollment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'incrementEnrollment'");
    }

    public void decrementEnrollment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decrementEnrollment'");
    }

    public void notifyWaitlist() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyWaitlist'");
    }

    public void removeFromWaitlist(Student student) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeFromWaitlist'");
    }

}