package org.cityuhk.CourseRegistrationSystem.Service;

import java.time.LocalDateTime;
import java.util.UUID;

public class LoginSession {

	private UUID sessionId;
	private String userEID;
	private LocalDateTime validUtil;

	public LoginSession(String userEID) {
		this.sessionId =  UUID.randomUUID();
		this.userEID = userEID;

	}
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(validUtil);
	}

	public UUID getSessionId() {
		return sessionId;
	}
	
	public String getUserEID() {
		return userEID;
	}
}
