package ttt_lobby_service.infrastructure;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

import common.exagonal.Adapter;

import java.net.http.HttpResponse;

import io.vertx.core.json.JsonObject;
import ttt_lobby_service.application.CreateGameFailedException;
import ttt_lobby_service.application.GameAlreadyPresentException;
import ttt_lobby_service.application.GameService;
import ttt_lobby_service.application.InvalidJoinGameException;
import ttt_lobby_service.application.JoinGameFailedException;
import ttt_lobby_service.application.ServiceNotAvailableException;
import ttt_lobby_service.domain.TTTSymbol;
import ttt_lobby_service.domain.UserId;

@Adapter
public class GameServiceProxy implements GameService {

	private String serviceURI;
	
	public GameServiceProxy(String serviceAPIEndpoint)  {
		this.serviceURI = serviceAPIEndpoint;		
	}
	
	@Override
	public void createNewGame(String gameId) throws GameAlreadyPresentException, CreateGameFailedException, ServiceNotAvailableException {
		    HttpClient client = HttpClient.newHttpClient();
            JsonObject body = new JsonObject();
            body.put("gameId", gameId);
            		
            String gameResEndpoint = serviceURI + "/api/v1/games";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gameResEndpoint))
                    .header("Accept", "application/json")
                    .POST(BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = null;
            try {
            	response = client.send(request, HttpResponse.BodyHandlers.ofString());
            	System.out.println("Response Code: " + response.statusCode());
            } catch (Exception ex) {
            	ex.printStackTrace();
            	throw new CreateGameFailedException();
            }
            
            if (response.statusCode() == 200) {
                JsonObject json = new JsonObject(response.body());	                               
                var res = json.getString("result");
                if (res.equals("error")) {
                	var err = json.getString("error");
                	if (err.equals("game-already-present")) {
                		throw new GameAlreadyPresentException();
                	} else {
                		throw new CreateGameFailedException();
                	}
                }
            } else {
                System.out.println("POST request failed: " + response.body());
    			throw new ServiceNotAvailableException();
            }
		
	}
	

	@Override
	public String joinGame(UserId userId, String gameId, TTTSymbol symbol) throws InvalidJoinGameException, JoinGameFailedException, ServiceNotAvailableException {
	    HttpClient client = HttpClient.newHttpClient();
        JsonObject body = new JsonObject();
        body.put("userId", userId.id());
        body.put("symbol", symbol.equals(TTTSymbol.X) ? "X" : "O");
        		
        String joinGameEndpoint = serviceURI + "/api/v1/games/" + gameId + "/join";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(joinGameEndpoint))
                .header("Accept", "application/json")
                .POST(BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = null;
        try {
        	response = client.send(request, HttpResponse.BodyHandlers.ofString());
        	System.out.println("Response Code: " + response.statusCode());
        } catch (Exception ex) {
        	ex.printStackTrace();
        	throw new JoinGameFailedException();
        }
        if (response.statusCode() == 200) {
            JsonObject json = new JsonObject(response.body());	                               
            var res = json.getString("result");
            if (res.equals("ok")) {
				var playerSessionId = json.getString("playerSessionId");
				return playerSessionId;
 		    } else if (res.equals("error")) {
 		    	throw new InvalidJoinGameException();
            } else {
            	throw new JoinGameFailedException();
            }
        } else {
            System.out.println("POST request failed: " + response.body());
			throw new ServiceNotAvailableException();
        }
	}
	


}
