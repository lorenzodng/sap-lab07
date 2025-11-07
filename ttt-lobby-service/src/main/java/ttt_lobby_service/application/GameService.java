package ttt_lobby_service.application;

import common.exagonal.OutBoundPort;
import ttt_lobby_service.domain.*;

@OutBoundPort
public interface GameService  {

	void createNewGame(String gameId) throws GameAlreadyPresentException, CreateGameFailedException, ServiceNotAvailableException;
	
	String joinGame(UserId userId, String gameId, TTTSymbol symbol) throws InvalidJoinGameException, JoinGameFailedException, ServiceNotAvailableException;
	    
}
