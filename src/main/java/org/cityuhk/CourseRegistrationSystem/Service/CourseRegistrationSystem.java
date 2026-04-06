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
	 * @param sessionId
	 */
	public void registerSection(String studentId, String courseCode, int sessionId) {
		// TODO - implement CourseRegistrationSystem.registerSection
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param studentId
	 * @param courseCode
	 * @param sessionId
	 */
	public void dropSection(String studentId, String courseCode, int sessionId) {
		// TODO - implement CourseRegistrationSystem.dropSection
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param stduentId
	 * @param coursecode
	 * @param sessionId
	 */
	public void joinSectionWaitlist(String stduentId, String coursecode, int sessionId) {
		// TODO - implement CourseRegistrationSystem.joinSectionWaitlist
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param studentId
	 * @param courseCode
	 * @param sectionIds
	 * @param sessionId
	 */
	public int submitPlan(String studentId, ArrayList<String> courseCode, ArrayList<Integer> sectionIds, int sessionId) {

	}

	/**
	 * 
	 * @param studentId
	 * @param sessionId
	 */
	public ArrayList<RegistrationPlan> getPlanByStudentId(String studentId, int sessionId) {

	}

	public ArrayList<Course> getSectionList() {

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

}