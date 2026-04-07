package org.cityuhk.CourseRegistrationSystem.Service;
public class SessionManager { 
//assigning a session to a logged in account so the system knows which account requires a specific operation(e.g. add class)
// some operation need specific permissions, for example, only logged in students can add/drop section, only logged in admins can manage users
// everytime a user log in, the system generates a new session, each session has a unique session id
// the users browser will store and use the sessionid when requesting from the server, the server then use the session id to know what type of user is requesting
// this makes sure that users that hasnt logged in or dont have permission cannot access things that need higher permission

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