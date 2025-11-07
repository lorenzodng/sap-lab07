package ttt_lobby_service.application;

import java.util.HashMap;
import java.util.logging.Logger;

import common.ddd.Repository;

public class UserSessions implements Repository {
	static Logger logger = Logger.getLogger("[SessionRepo]");

	private HashMap<String, UserSession> userSessions;
	
	public UserSessions() {
		userSessions = new HashMap<>();
	}

	public boolean isPresent(String sessionId) {
		return userSessions.containsKey(sessionId);
	}
	
	public void addSession(UserSession us) {
		userSessions.put(us.getSessionId(), us);
	}

	public UserSession getSession(String sessionId) {
		return userSessions.get(sessionId);
	}
	
}
