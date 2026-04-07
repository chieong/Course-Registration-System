package org.cityuhk.CourseRegistrationSystem.Service;
public class SessionManager { //assigning a session to a logged in account so the system knows which account requires a specific operation(e.g. add class)

	private SessionManager instance;


	/**
	 * 
	 * @param sessionId
	 * @param userEID
	 */
	public String getSessionUserEID(int sessionId) {
		
	}

	/**
	 * 
	 * @param userEID
	 * @param password
	 */
	public int newSession(String userEID, String password) {
		// TODO - implement SessionManager.newSession
		throw new UnsupportedOperationException();
	}

	private SessionManager() {
		// TODO - implement SessionManager.SessionManager
		throw new UnsupportedOperationException();
	}

	private SessionManager getInstance() {
		return this.instance;
	}

}