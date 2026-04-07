public class SessionManager {

	private SessionManager instance;

	/**
	 * 
	 * @param sessionId
	 * @param userEID
	 */
	public boolean validateSession(int sessionId, String userEID) {

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