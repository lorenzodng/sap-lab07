package ttt_lobby_service.application;

import ttt_lobby_service.domain.UserId;

/**
 * 
 * Representing a user session.
 * 
 * - Created when a user logs in
 * - It includes the operations that a user can do -  create a game, join a game
 * 
 */
public class UserSession {

	private String sessionId;
	private UserId userId;
	private LobbyService lobbyService;
	
	public UserSession(String sessionId, UserId userId, LobbyService lobby) {
		this.userId = userId;
		this.lobbyService = lobby;
		this.sessionId = sessionId;
	}
		
	/*
	public void createNewGame(String gameId) throws GameAlreadyPresentException {
		gameService.createNewGame(gameId);		
	}

	public PlayerSession joinGame(String gameId, TTTSymbol symbol, PlayerSessionEventObserver notifier) throws InvalidJoinException {
		return gameService.joinGame(userId, gameId, symbol, notifier);
	}
	*/
	
	public String getSessionId() {
		return sessionId;
	}
	
	public UserId getUserId() {
		return userId;
	}

}
