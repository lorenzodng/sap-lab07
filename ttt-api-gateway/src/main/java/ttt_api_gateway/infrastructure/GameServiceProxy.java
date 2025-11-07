package ttt_api_gateway.infrastructure;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import ttt_api_gateway.application.*;
import ttt_api_gateway.domain.Game;

public class GameServiceProxy extends HTTPSyncBaseProxy implements GameService  {

	private String serviceAddress;

	public GameServiceProxy(String serviceAPIEndpoint) {
		this.serviceAddress = serviceAPIEndpoint;
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

	
}
