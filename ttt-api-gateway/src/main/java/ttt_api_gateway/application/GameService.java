package ttt_api_gateway.application;

import common.exagonal.InBoundPort;
import ttt_api_gateway.domain.*;

/**
 * 
 * Interface of the Game Service at the application layer
 * 
 */
@InBoundPort
public interface GameService  {


	/**
     * 
     * Get game info.
     * 
     * @param gameId
     * @return
     * @throws AccountNotFoundException
     */
	Game getGameInfo(String gameId) throws GameNotFoundException, ServiceNotAvailableException;
		
	/**
	 * Make a new move
	 * 
	 * @param gameId
	 * @param playerSessionId
	 * @param x
	 * @param y
	 */
	void makeAMove(String gameId, String playerSessionId, int x, int y) throws InvalidMoveException, ServiceNotAvailableException;


    
}
