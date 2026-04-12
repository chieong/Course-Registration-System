package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
import java.util.UUID;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CourseRegistrationSystem {

	private final ICourseRepository courseRepository;
	private final IRegistrationPeriodRepository registrationPeriodRepo;
	private final IUserRepository userRepository;
	private final RegistrationManager registrationManager;
	private final RegistrationPlanManager registrationPlanManager;
	private final SessionManager sessionManager;


        @Autowired
        public CourseRegistrationSystem(
                ICourseRepository courseRepository,
                IRegistrationPeriodRepository registrationPeriodRepo,
                IUserRepository userRepository,
                RegistrationManager registrationManager,
                RegistrationPlanManager registrationPlanManager,
                SessionManager sessionManager
                ) {
            this.courseRepository = courseRepository;
            this.registrationManager = registrationManager;
            this.registrationPeriodRepo = registrationPeriodRepo;
            this.registrationPlanManager = registrationPlanManager;
            this.userRepository = userRepository;
            this.sessionManager = sessionManager;
        }

	/**
	 * Return null if the userEID or password is incorrect
	 * @param userEID 
	 * @param password
	 */
	public UUID newSession(String userEID, String password) {
		return sessionManager.createNewSession(userEID, password);
	}

	/**
	 * 
	 * @param courseCode
	 * @param sectionId
	 */
	public void addSection(String courseCode, int sectionId, int sessionId) {
		//add todo
		throw new UnsupportedOperationException();

	}

	/**
	 * 
	 * @param courseCode
	 * @param sectionId
	 */
	public void dropSection(String courseCode, int sectionId, int sessionId) {
		//add todo
		throw new UnsupportedOperationException();

	}

	/**
	 * 
	 * @param coursecode
	 * @param sectionId
	 */
	public void joinSectionWaitlist(String coursecode, int sectionId) {
		//add todo
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param courseCode
	 * @param sectionIds
	 * @param sessionId
	 */
	public int submitPlan(ArrayList<String> courseCode, ArrayList<Integer> sectionIds, int sessionId) {
		// TODO - implement CourseRegistrationSystem.getSectionList
		throw new UnsupportedOperationException();

	}

	/**
	 * 
	 * @param sessionId
	 */
	public ArrayList<RegistrationPlan> getPlanByStudentId(int sessionId) {
		// TODO - implement CourseRegistrationSystem.getSectionList
		throw new UnsupportedOperationException();

	}

	public ArrayList<Course> getSectionList() {
		// TODO - implement CourseRegistrationSystem.getSectionList
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param sessionId
	 */
	public ArrayList<RegistrationRecord> checkTimeTable(int sessionId) {
		// TODO - implement CourseRegistrationSystem.getSectionList
		throw new UnsupportedOperationException();

	}

	/**
	 * 
	 * @param classId
	 * @param sessionId
	 */
	public ArrayList<Student> checkStudentList(int classId, int sessionId) {
		// TODO - implement CourseRegistrationSystem.getSectionList
		throw new UnsupportedOperationException();

	}

	/**
	 * 
	 * @param courseCode
	 * @param courseTitle
	 * @param credits
	 * @param description
	 * @param exclusiveCourseCode
	 * @param sessionId
	 */
	public void addCourse(String courseCode, String courseTitle, int credits, String description, ArrayList<String> prerequisiteCourseCode, ArrayList<String> exclusiveCourseCode, int sessionId) {
		// TODO - implement CourseRegistrationSystem.addCourse
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param courseCode
	 * @param courseTitle
	 * @param credits
	 * @param description
	 * @param exclusiveCourseCode
	 * @param sessionId
	 */
	public void modifyCourse(String courseCode, String courseTitle, int credits, String description, ArrayList<String> prerequisiteCourseCode, ArrayList<String> exclusiveCourseCode, int sessionId) {
		// TODO - implement CourseRegistrationSystem.modifyCourse
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param courseCode
	 * @param sessionId
	 */
	public Course removeCourse(String courseCode, int sessionId) {
		// TODO - implement CourseRegistrationSystem.removeCourse
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param cohort
	 * @param starteDateTime
	 * @param endDateTime
	 * @param sessionId
	 */
	public int setRegistrationPeriod(int cohort, String starteDateTime, int endDateTime, int sessionId) {
		// TODO - implement CourseRegistrationSystem.setRegistrationPeriod
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param registrationPeriodId
	 */
	public void removeRegistrationPeriod(int registrationPeriodId) {
		// TODO - implement CourseRegistrationSystem.removeRegistrationPeriod
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param registrationPeriodId
	 * @param cohort
	 * @param starteDateTime
	 * @param endDateTime
	 * @param sessionId
	 */
	public void modifyRegistrationPeriod(int registrationPeriodId, int cohort, String starteDateTime, int endDateTime, int sessionId) {
		// TODO - implement CourseRegistrationSystem.modifyRegistrationPeriod
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param UserEID
	 * @param name
	 * @param studentId
	 * @param cohort
	 * @param major
	 * @param department
	 * @param minCredit
	 * @param maxCredit
	 */
	public void createNewStudent(String UserEID, String name, int studentId, int cohort, String major, String department, int minCredit, int maxCredit) {
		// TODO - implement CourseRegistrationSystem.createNewStudent
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param UserEID
	 * @param name
	 * @param studentId
	 * @param cohort
	 * @param major
	 * @param department
	 * @param minCredit
	 * @param maxCredit
	 */
	public void modifyUser(String UserEID, String name, int studentId, int cohort, String major, String department, int minCredit, int maxCredit) {
		// TODO - implement CourseRegistrationSystem.modifyUser
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param UserEID
	 */
	public void deleteUser(String UserEID) {
		// TODO - implement CourseRegistrationSystem.deleteUser
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param studentId
	 */
	public boolean isRegistrationPeriodOpen(int studentId) {
		// TODO - implement CourseRegistrationSystem.isRegistrationPeriodOpen
		throw new UnsupportedOperationException();
	}

}
