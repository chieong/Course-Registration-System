package org.cityuhk.CourseRegistrationSystem.Service;

import java.time.LocalDateTime;
import java.util.UUID;

public class LoginSession {

	private UUID sessionId;
	private String userEID;
	private LocalDateTime validUtil;

	public LoginSession(String userEID) {
		this.sessionId =  UUID.fromString(userEID);
		this.userEID = userEID;

	}
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(validUtil);
	}

	public String getSessionId() {
		return sessionId.toString();
	}
	
	public String getUserEID() {
		return userEID;
	}
}