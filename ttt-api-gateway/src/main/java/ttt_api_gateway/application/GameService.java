package ttt_api_gateway.application;

import common.exagonal.OutBoundPort;
import io.vertx.core.Vertx;
import ttt_api_gateway.domain.*;

/**
 * 
 * Interface for interacting with the Game Service
 * 
 */
@OutBoundPort
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


	/**
	 * 
	 * Create an event channel to receive game events, asynchronously 
	 * 
	 * @param playerSessionId
	 * @param vertx
	 */
	void createAnEventChannel(String playerSessionId, Vertx vertx);		

    
}
