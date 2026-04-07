package org.cityuhk.CourseRegistrationSystem.Service;
import java.util.ArrayList;
public class SessionManager { 
//assigning a session to a logged in account so the system knows which account requires a specific operation(e.g. add class)
// some operation need specific permissions, for example, only logged in students can add/drop section, only logged in admins can manage users
// everytime a user log in, the system generates a new session, each session has a unique session id
// the users browser will store and use the sessionid when requesting from the server, the server then use the session id to know what type of user is requesting
// this makes sure that users that hasnt logged in or dont have permission cannot access things that need higher permission

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

	/**
	 * 
	 * @param user
	 */
	public void getTimeTable(IAcademic user) {
		// TODO - implement RegistrationManager.getTimeTable
		// TimeTable is stored at section(ArrayList<IStudent> enRolledStudent), but should it?
		// if we wanted to know one user's timetable,
		// we have to loop through all section's enrolled students

		throw new UnsupportedOperationException();
	}

}