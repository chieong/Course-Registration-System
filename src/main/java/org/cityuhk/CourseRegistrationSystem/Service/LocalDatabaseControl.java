public class LocalDatabaseControl implements ICourseRepository, IRegistrationRecordRepository, IRegistrationPlanRepository, IRegistrationPeriodRepository, ICredentialRepository, IUserRepository {

	/**
	 * 
	 * @param courseId
	 */
	public Course getCourseById(String courseId) {
		// TODO - implement LocalDatabaseControl.getCourseById
		throw new UnsupportedOperationException();
	}

	public ArrayList<Course> getAllCourse() {

	}

	/**
	 * 
	 * @param course
	 */
	public void createCourse(Course course) {
		// TODO - implement LocalDatabaseControl.createCourse
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param courseId
	 * @param updatedCourse
	 */
	public void updateCourse(String courseId, Course updatedCourse) {
		// TODO - implement LocalDatabaseControl.updateCourse
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param courseId
	 */
	public void removerCourse(String courseId) {
		// TODO - implement LocalDatabaseControl.removerCourse
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param record
	 */
	public void addRegistrationRecord(RegistrationRecord record) {
		// TODO - implement LocalDatabaseControl.addRegistrationRecord
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param studentId
	 */
	public ArrayList<RegistrationRecord> getStudentRegistrationRecordByStudent(int studentId) {
		// TODO - implement LocalDatabaseControl.getStudentRegistrationRecordByStudent
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param sectionId
	 */
	public void pollNextFromWaitlist(id sectionId) {
		// TODO - implement LocalDatabaseControl.pollNextFromWaitlist
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param rp
	 */
	public int addPlanRecord(RegistrationPlan rp) {
		// TODO - implement LocalDatabaseControl.addPlanRecord
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param planId
	 */
	public RegistrationPlan getPlanById(int planId) {
		// TODO - implement LocalDatabaseControl.getPlanById
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param rp
	 */
	public int addRegistrationPeriod(RegistrationPeriod rp) {
		// TODO - implement LocalDatabaseControl.addRegistrationPeriod
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param registrationPeriodID
	 */
	public void removeRegistrationPeriod(int registrationPeriodID) {
		// TODO - implement LocalDatabaseControl.removeRegistrationPeriod
		throw new UnsupportedOperationException();
	}

	public ArrayList<RegistrationPeriod> getAllRegistrationPeriod() {
		// TODO - implement LocalDatabaseControl.getAllRegistrationPeriod
		throw new UnsupportedOperationException();
	}

	public boolean validateCredential() {
		// TODO - implement LocalDatabaseControl.validateCredential
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param userEID
	 */
	public User getUser(String userEID) {
		// TODO - implement LocalDatabaseControl.getUser
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param user
	 */
	public void createUser(User user) {
		// TODO - implement LocalDatabaseControl.createUser
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param userEID
	 * @param user
	 */
	public void modifyUser(String userEID, User user) {
		// TODO - implement LocalDatabaseControl.modifyUser
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param userEID
	 */
	public void deleteUser(String userEID) {
		// TODO - implement LocalDatabaseControl.deleteUser
		throw new UnsupportedOperationException();
	}

}