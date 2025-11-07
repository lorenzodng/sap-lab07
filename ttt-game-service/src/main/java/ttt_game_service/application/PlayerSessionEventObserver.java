package ttt_game_service.application;

import java.util.Optional;

import common.exagonal.OutBoundPort;

/**
 * 
 * This is an outbound port to notify events to players
 * playing a game
 * 
 */
@OutBoundPort
public interface PlayerSessionEventObserver {

	/**
	 * 
	 * Enable the dispatching of events
	 * 
	 * This occurs when the observer is ready to receive the events
	 * 
	 * @param playerSessionId
	 */
	void enableEventNotification(String playerSessionId);
	
	/**
	 * 
	 * Notify that the game started
	 * 
	 */
	void gameStarted(String playerSessionId);

	/**
	 * 
	 * Notify that a player did a new move
	 * 
	 * @param symbol
	 * @param x
	 * @param y
	 */
	void newMove(String playerSessionId, String symbol, int x, int y);
	
	/**
	 * 
	 * Notify that the game ended
	 * 
	 * @param winner
	 */
	void gameEnded(String playerSessionId, Optional<String> winner);
}
