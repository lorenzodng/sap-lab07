package ttt_lobby_service.infrastructure;

import io.vertx.core.Vertx;
import ttt_lobby_service.application.*;

public class LobbyServiceMain {

	static final int LOBBY_SERVICE_PORT = 9001;
	
	/*
	 * The URI of the services refers to the logic name
	 * resolved by the Docker network infrastructure,
	 * as defined in the docker-compose.yaml file
	 */
	static final String ACCOUNT_SERVICE_ADDRESS = "http://account-service:9000";
	static final String GAME_SERVICE_ADDRESS = "http://game-service:9002";

	/* addresses when not deployed using Docker */
	// static final String ACCOUNT_SERVICE_ADDRESS = "http://localhost:9000";
	// static final String GAME_SERVICE_ADDRESS = "http://localhost:9002";

	public static void main(String[] args) {
		
		var lobby = new LobbyServiceImpl();
		
		AccountService accountService =  new AccountServiceProxy(ACCOUNT_SERVICE_ADDRESS);
		lobby.bindAccountService(accountService);

		GameService gameService =  new GameServiceProxy(GAME_SERVICE_ADDRESS);
		lobby.bindGameService(gameService);
		
		var vertx = Vertx.vertx();
		var server = new LobbyServiceController(lobby, LOBBY_SERVICE_PORT, GAME_SERVICE_ADDRESS);
		vertx.deployVerticle(server);	
	}

}

