package ttt_game_service.infrastructure;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import common.exagonal.Adapter;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import ttt_game_service.application.PlayerSessionEventObserver;

/**
 * 
 * This is implementation of a player session game event observer,
 * based on Vert.x 
 * 
 * It dispatches the observed events on the Event Bus,
 * so that the game service controller could consume it and
 * send to the players, connected using a web socket.
 * 
 * It is an adapted of the PlayerSessionEventObserver port
 */
@Adapter
public class VertxPlayerSessionEventObserver implements PlayerSessionEventObserver {
	static Logger logger = Logger.getLogger("[VertxEventNotifierAdapter]");

	private EventBus eventBus;
	private List<JsonObject> eventBuffer;
	private boolean channelOnBusReady;
	
	public VertxPlayerSessionEventObserver(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBuffer = new LinkedList<JsonObject>();
		channelOnBusReady = false; 
	}
	
	public void enableEventNotification(String playerSessionId) {
		channelOnBusReady = true;
		for (var ev: eventBuffer) {
			eventBus.publish(playerSessionId, ev);
		}
		eventBuffer.clear();
	}
	
	public void gameStarted(String playerSessionId) {
		logger.info("game-started for " + playerSessionId);
		var evStarted = new JsonObject();
		evStarted.put("event", "game-started");
		if (channelOnBusReady) {
			eventBus.publish(playerSessionId, evStarted);
		} else {
			eventBuffer.add(evStarted);
		}
	}					
	
	public void newMove(String playerSessionId, String who, int x, int y) {
		var evMove = new JsonObject();
		evMove.put("event", "new-move");
		evMove.put("x", x);
		evMove.put("y", y);
		evMove.put("symbol", who);
		if (channelOnBusReady) {
			eventBus.publish(playerSessionId, evMove);
		} else {
			eventBuffer.add(evMove);
		}
	}

	public void gameEnded(String playerSessionId, Optional<String> winner) {
		var evEnd = new JsonObject();
		evEnd.put("event", "game-ended");
		if (winner.isEmpty()) {
			evEnd.put("result", "tie");					
		} else {
			evEnd.put("winner", winner.get());											
		}				
		if (channelOnBusReady) {
			eventBus.publish(playerSessionId, evEnd);
		} else {
			eventBuffer.add(evEnd);
		}
	}					
	
}
