package ttt_api_gateway.infrastructure;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import common.exagonal.Adapter;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import ttt_api_gateway.application.*;
import ttt_api_gateway.domain.Game;

/**
 * 
 * Proxy for GameService, using sync HTTP
 * 
 */
@Adapter
public class GameServiceProxy extends HTTPSyncBaseProxy implements GameService  {

	private String serviceAddress;
	private int wsPort;
	private String wsAddress;

	public GameServiceProxy(String serviceAPIEndpoint) {
		this.serviceAddress = serviceAPIEndpoint;
	}

	public GameServiceProxy(String serviceAPIEndpoint, String wsAddress, int wsPort) {
		this.serviceAddress = serviceAPIEndpoint;
		this.wsPort = wsPort;
		this.wsAddress = wsAddress;
	}

	@Override
	public void makeAMove(String gameId, String playerSessionId, int x, int y) throws InvalidMoveException, ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject();
			body.put("x", x);
			body.put("y", y);
			HttpResponse<String> response = doPost( serviceAddress + "/api/v1/games/" + gameId + "/" + playerSessionId + "/move", body);			
			if (response.statusCode() == 200) {
				return ;
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

	@Override
	public Game getGameInfo(String gameId) throws GameNotFoundException, ServiceNotAvailableException {
		try {
			HttpResponse<String> response = doGet( serviceAddress + "/api/v1/games/" + gameId);			
			if (response.statusCode() == 200) {
				JsonObject json = new JsonObject(response.body());
				JsonObject obj = json.getJsonObject("gameInfo");
				JsonArray bs = obj.getJsonArray("boardState");
				List<String> l = new ArrayList<String>();
				for (var el: bs) {
					l.add(el.toString());
				}
				return new Game(obj.getString("gameId"), obj.getString("gameState"), l, obj.getString("turn"));
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}
	
	
	@Override
	public void createAnEventChannel(String playerSessionId, Vertx vertx) {		
		var eb = vertx.eventBus();
		WebSocketClient client = vertx.createWebSocketClient();
		client
		  .connect(wsPort, wsAddress, "/api/v1/events")
		  .onSuccess(ws -> {
			  System.out.println("Connected!");

			  ws.textMessageHandler(msg -> {
				  eb.publish(playerSessionId, msg); 
			  });
			  
			  /* first message */
		      
		      JsonObject obj = new JsonObject();
		      obj.put("playerSessionId", playerSessionId);		      
		      ws.writeTextMessage(obj.toString());
		  })
		  .onFailure(err -> {
			  eb.publish(playerSessionId, "error");
		  });
	}

	
}
