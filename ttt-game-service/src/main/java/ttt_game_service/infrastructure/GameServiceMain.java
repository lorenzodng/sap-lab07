package ttt_game_service.infrastructure;

import io.vertx.core.Vertx;
import ttt_game_service.application.*;

public class GameServiceMain {

	static final int GAME_SERVICE_PORT = 9002;

	public static void main(String[] args) {
		
		var service = new GameServiceImpl();
				
		service.bindGameRepository(new InMemoryGameRepository());
		
		var vertx = Vertx.vertx();
		var server = new GameServiceController(service, GAME_SERVICE_PORT);
		vertx.deployVerticle(server);	
	}

}

