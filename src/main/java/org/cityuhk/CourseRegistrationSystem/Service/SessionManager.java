package org.cityuhk.CourseRegistrationSystem.Service;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionManager { 
//assigning a session to a logged in account so the system knows which account requires a specific operation(e.g. add class)
// some operation need specific permissions, for example, only logged in students can add/drop section, only logged in admins can manage users
// everytime a user log in, the system generates a new session, each session has a unique session id
// the users browser will store and use the sessionid when requesting from the server, the server then use the session id to know what type of user is requesting
// this makes sure that users that hasnt logged in or dont have permission cannot access things that need higher permission

	private Map<UUID, LoginSession> loginSessions;
	private ICredentialRepository credentialRepository;

        @Autowired
	public SessionManager(ICredentialRepository credentialRepository) {
            this.credentialRepository = credentialRepository;
            loginSessions = new Hashtable<>();
	}

	/**
	 * Return the userEID belongs to the sessionId, return null if
	 * @param sessionId
	 * @param userEID
	 */
	public String getSessionUserEID(UUID sessionId) {
            return loginSessions.get(sessionId).getUserEID();
	}

	/**
	 * 
	 * @param userEID
	 * @param password
	 */
	public UUID createNewSession(String userEID, String password) {
		boolean isValid = credentialRepository.validateCredential(userEID,password);
		if (isValid) {
			LoginSession e = new LoginSession(userEID);
			loginSessions.put(e.getSessionId(), e);
			return e.getSessionId();
		} else {
			return null;
		}
	}

}
