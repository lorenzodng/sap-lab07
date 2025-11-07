package ttt_game_service.application;

import java.util.HashMap;
import java.util.logging.Logger;

import common.ddd.Aggregate;
import common.ddd.Repository;

/**
 * 
 * Player sessions.
 * 
 */
public class PlayerSessions implements Repository {
	static Logger logger = Logger.getLogger("[PlayerSessionRepo]");

	private HashMap<String, PlayerSession> userSessions;
	
	public PlayerSessions() {
		userSessions = new HashMap<>();
	}
	
	public void addSession(PlayerSession ps) {
		userSessions.put(ps.getId(), ps);
	}

	public PlayerSession getSession(String sessionId) {
		return userSessions.get(sessionId);
	}
	
}
