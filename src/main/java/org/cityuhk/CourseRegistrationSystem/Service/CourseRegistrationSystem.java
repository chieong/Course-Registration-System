public class CourseRegistrationSystem {

	/**
	 * 
	 * @param userEID
	 * @param password
	 */
	public int newSession(String userEID, String password) {
		// TODO - implement CourseRegistrationSystem.newSession
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param studentId
	 * @param courseCode
	 * @param sectionId
	 */
	public void addSection(String studentId, String courseCode, int sectionId) {

	}

	/**
	 * 
	 * @param studentId
	 * @param courseCode
	 * @param sectionId
	 */
	public void dropSection(String studentId, String courseCode, int sectionId) {

	}

	/**
	 * 
	 * @param stduentId
	 * @param coursecode
	 * @param sectionId
	 */
	public void joinSectionWaitlist(String stduentId, String coursecode, int sectionId) {

	}

	/**
	 * 
	 * @param studentId
	 * @param courseCode
	 * @param sectionIds
	 * @param sessionId
	 */
	public int submitPlan(String studentId, ArrayList<String> courseCode, ArrayList<Integer> sectionIds, int sessionId) {
		// TODO - implement CourseRegistrationSystem.submitPlan
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param studentId
	 * @param sessionId
	 */
	public ArrayList<RegistrationPlan> getPlanByStudentId(String studentId, int sessionId) {
		// TODO - implement CourseRegistrationSystem.getPlanByStudentId
		throw new UnsupportedOperationException();
	}

	public ArrayList<Course> getSectionList() {
		// TODO - implement CourseRegistrationSystem.getSectionList
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param userId
	 * @param sessionId
	 */
	public TimeTable checkTimeTable(int userId, int sessionId) {
		// TODO - implement CourseRegistrationSystem.checkTimeTable
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param staffId
	 * @param classId
	 * @param sessionId
	 */
	public ArrayList<Student> checkStudentList(int staffId, int classId, int sessionId) {
		// TODO - implement CourseRegistrationSystem.checkStudentList
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
	public void addCourse(String courseCode, String courseTitle, int credits, String description, ArrayList<String> exclusiveCourseCode, int sessionId) {
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
	public void modifyCourse(String courseCode, String courseTitle, int credits, String description, ArrayList<String> exclusiveCourseCode, int sessionId) {
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

}