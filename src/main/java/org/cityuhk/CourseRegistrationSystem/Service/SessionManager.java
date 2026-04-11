package org.cityuhk.CourseRegistrationSystem.Service;
import java.util.ArrayList;
public class SessionManager { 
	private static SessionManager instance = new SessionManager();
	private ArrayList<LoginSession> loginSessions;
	private ArrayList<SessionObserver> observers = new ArrayList<>();
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
			if (session.getSessionId().equals(sessionId)) {
				if (session.isExpired()) {
					loginSessions.remove(session); 
					return null;
				}
				return session.getUserEID();
			}
		}
		return null; 	
	}

	public void addObserver(SessionObserver observer) {
        observers.add(observer);
    }

	public void destroySession(String sessionId) {
		LoginSession toRemove = null;
		for (LoginSession session : loginSessions) {
			if (session.getSessionId().equals(sessionId)) {
				toRemove = session;
				break;
			}
		}
		if (toRemove != null) {
			loginSessions.remove(toRemove);
			for (SessionObserver obs : observers) {
				obs.onUserLogout(toRemove.getUserEID());
			}
		}
	}

    public String createNewSession(String userEID, String password) {
        boolean isValid = credentialDB.validateCredential(userEID, password);
        if (isValid) {
            LoginSession e = new LoginSession(userEID);
            loginSessions.add(e);
            
            for (SessionObserver obs : observers) {
                obs.onUserLogin(userEID);
            }
            return e.getSessionId();
        }
        return null;
    }
}

}