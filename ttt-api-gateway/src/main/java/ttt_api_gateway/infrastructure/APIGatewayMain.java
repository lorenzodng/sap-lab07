package ttt_api_gateway.infrastructure;

import io.vertx.core.Vertx;
import ttt_api_gateway.application.*;

/*
server api-gateway
flusso di chiamate: client -> server api-gateway -> ....
 */
public class APIGatewayMain {

	static final int BACKEND_PORT = 8080; //porta sul quale il server ascolta le richiesta http

    //anche se il client contatta probabilmente il servizio di gioco solo dopo avere contattato il servizio lobby e/o il servizio degli account, per sicurezza dei possibili scenari di interazioni si collega l'api-gateway con tutti (nello scenario "peggiore", comunque non influisce sul carico computazionale)
    static final String LOBBY_SERVICE_ADDRESS = "http://lobby-service:9001"; //link del servizio di lobby ("lobby-service" è il nome del container di lobby)
    static final String ACCOUNT_SERVICE_ADDRESS = "http://account-service:9000"; //link del servizio degli account ("account-service" è il nome del container degli account)
	static final String GAME_SERVICE_ADDRESS = "http://game-service:9002"; //link del servizio degli account ("game-service" è il nome del container di gioco)
	static final String GAME_SERVICE_WS_ADDRESS = "game-service"; //indirizzo (nome) del servizio di gioco
	static final int GAME_SERVICE_WS_PORT = 9002; //porta del servizio di gioco

	public static void main(String[] args) {

        LobbyService lobbyService = new LobbyServiceProxy(LOBBY_SERVICE_ADDRESS); //crea un'istanza del proxy di lobby
        AccountService accountService = new AccountServiceProxy(ACCOUNT_SERVICE_ADDRESS); //crea un'istanza del proxy degli account
		GameService gameService = new GameServiceProxy(GAME_SERVICE_ADDRESS, GAME_SERVICE_WS_ADDRESS, GAME_SERVICE_WS_PORT); //crea un'istanza del proxy di gioco
		var vertx = Vertx.vertx(); //crea un'istanza vertx per gestire le richieste http
		var server = new APIGatewayController(accountService, lobbyService, gameService, BACKEND_PORT); //crea un'istanza del controller
		vertx.deployVerticle(server); //avvia il server sulla porta specificata (esegue il metodo "start" del controller)
	}
}

