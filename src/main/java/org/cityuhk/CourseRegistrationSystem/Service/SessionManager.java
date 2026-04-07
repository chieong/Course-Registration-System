package org.cityuhk.CourseRegistrationSystem.Service;
<<<<<<< HEAD

import java.util.ArrayList;

public class SessionManager {
=======
public class SessionManager { //assigning a session to a logged in account so the system knows which account requires a specific operation(e.g. add class)
>>>>>>> de9e70d762dff4e4e3821bc806d62dd3f551769e

	private static SessionManager instance = new SessionManager();
	private ArrayList<LoginSession> loginSessions;
	private ICredentialRepository credentialDB;

	private SessionManager() {
		loginSessions = new ArrayList<>();
	}

	public static SessionManager getInstance() {
		return instance;
	}


	/**
	 * Return the userEID belongs to the sessionId, return null if
	 * @param sessionId
	 * @param userEID
	 */
	public String getSessionUserEID(String sessionId) {
		for (LoginSession session : loginSessions) {
			if (session.getSessionId() == sessionId) {
				if (session.isExpired()) {
					loginSessions.remove(session); 
					return null;
				}
				return session.getUserEID();
			}
		}
		return null; 	
	}

	/**
	 * 
	 * @param userEID
	 * @param password
	 */
	public String createNewSession(String userEID, String password) {
		boolean isValid = credentialDB.validateCredential(userEID,password);
		if (isValid) {
			LoginSession e = new LoginSession(userEID);
			loginSessions.add(e);
			return e.getSessionId();
		} else {
			return null;
		}
	}

}