package ttt_lobby_service.application;

import common.exagonal.InBoundPort;
import ttt_lobby_service.domain.TTTSymbol;

/**
 * 
 * Interface of the Game Lobby Service at the application layer
 * 
 */
@InBoundPort
public interface LobbyService  {

	String login(String userName, String password) throws LoginFailedException;
	
	void createNewGame(String sessionId, String gameId) throws CreateGameFailedException;
	
	String joinGame(String sessionId, String gameId, TTTSymbol symbol) throws JoinGameFailedException;
	
	
    
}
