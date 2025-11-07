package ttt_api_gateway.infrastructure;

import java.net.http.HttpResponse;

import common.exagonal.Adapter;
import io.vertx.core.json.JsonObject;
import ttt_api_gateway.application.CreateGameFailedException;
import ttt_api_gateway.application.JoinGameFailedException;
import ttt_api_gateway.application.LobbyService;
import ttt_api_gateway.application.LoginFailedException;
import ttt_api_gateway.application.ServiceNotAvailableException;
import ttt_api_gateway.application.TTTSymbol;

/**
 * 
 * Proxy for LobbyService, using synch HTTP 
 * 
 */
@Adapter
public class LobbyServiceProxy extends HTTPSyncBaseProxy implements LobbyService {

	private String serviceAddress;
	
	public LobbyServiceProxy(String serviceAPIEndpoint)  {
		this.serviceAddress = serviceAPIEndpoint;		
	}
	
	@Override
	public String login(String userName, String password) throws LoginFailedException, ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject();
			body.put("password", password);
			body.put("userName", userName);
			HttpResponse<String> response = doPost( serviceAddress + "/api/v1/lobby/login", body);			
			if (response.statusCode() == 200) {
				JsonObject json = new JsonObject(response.body());
				return json.getString("sessionId");
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

	@Override
	public void createNewGame(String sessionId, String gameId) throws CreateGameFailedException, ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject();
			body.put("gameId", gameId);
			HttpResponse<String> response = doPost( serviceAddress + "/api/v1/lobby/user-sessions/" + sessionId + "/create-game", body);			
			if (response.statusCode() == 200) {
				return;
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
		
	}

	@Override
	public String joinGame(String sessionId, String gameId, TTTSymbol symbol) throws JoinGameFailedException, ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject();
			body.put("gameId", gameId);
			body.put("symbol", symbol.toString());
			HttpResponse<String> response = doPost( serviceAddress + "/api/v1/lobby/user-sessions/" + sessionId + "/join-game", body);			
			if (response.statusCode() == 200) {
				JsonObject json = new JsonObject(response.body());
				return json.getString("playerSessionId");
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

}
