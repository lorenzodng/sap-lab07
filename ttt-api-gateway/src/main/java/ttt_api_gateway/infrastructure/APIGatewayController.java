package ttt_api_gateway.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.StaticHandler;
import ttt_api_gateway.application.*;

/*
controller tra l'api gateway e i proxy
flusso di chiamate: client -> server api-gateway -> controller -> ...
 */
public class APIGatewayController extends VerticleBase  {

	private int port; //porta su cui il server ascolta le richieste http
    static Logger logger = Logger.getLogger("[APIGatewayController]");
    static final String API_VERSION = "v1"; //versione dell'API utilizzata per definire le rotte

    //rotte per il servizio di lobby
    static final String LOGIN_RESOURCE_PATH = 			"/api/" + API_VERSION + "/lobby/login"; //rotta per effettuare il login di un account
    static final String USER_SESSIONS_RESOURCE_PATH = 	"/api/" + API_VERSION + "/lobby/user-sessions"; //rotta (di base) per le sessioni utente
    static final String CREATE_GAME_RESOURCE_PATH = 	"/api/" + API_VERSION + "/lobby/user-sessions/:sessionId/create-game"; //rotta per creare una nuova partita
    static final String JOIN_GAME_RESOURCE_PATH = 		"/api/" + API_VERSION + "/lobby/user-sessions/:sessionId/join-game"; //rotta per far entrare un utente in una partita

	//rotte per il servizio degli account
	static final String ACCOUNTS_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts"; //rotta per creare un nuovo account
	static final String ACCOUNT_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId"; //rotta per recuperare le informazioni di un account

    //rotte per il servizio di gioco
	static final String GAMES_RESOURCE_PATH = "/api/" + API_VERSION + "/games"; //rotta (di base) per recuperare le partite
	static final String GAME_RESOURCE_PATH =  GAMES_RESOURCE_PATH +   "/:gameId"; //rotta per recuperare le informazioni di una partita
	static final String PLAYER_MOVE_RESOURCE_PATH = GAME_RESOURCE_PATH + "/:playerSessionId/move"; //rotta per eseguire una mossa

    private LobbyService lobbyService; //servizio di lobby
    private AccountService accountService; //servizio degli account
    private GameService gameService; //servizio di gioco

	public APIGatewayController(AccountService accountService, LobbyService lobbyService, GameService gameService, int port) {
		this.port = port;
		logger.setLevel(Level.INFO);
		this.gameService = gameService;
		this.accountService = accountService;
		this.lobbyService = lobbyService;
	}

    //avvia il server
	public Future<?> start() {
		logger.log(Level.INFO, "TTT Game Service initializing...");
		HttpServer server = vertx.createHttpServer(); //crea un sever http

		Router router = Router.router(vertx); //router per l'instradamento delle richieste http
		router.route(HttpMethod.POST, ACCOUNTS_RESOURCE_PATH).handler(this::createNewAccount); //associa alla rotta per creare un nuovo account il relativo metodo
		router.route(HttpMethod.GET, ACCOUNT_RESOURCE_PATH).handler(this::getAccountInfo); //associa alla rotta per recuperare le informazioni di un account il relativo metodo
		router.route(HttpMethod.POST, LOGIN_RESOURCE_PATH).handler(this::login); //associa alla rotta per effettuare il login di un account il relativo metodo
		router.route(HttpMethod.POST, CREATE_GAME_RESOURCE_PATH).handler(this::createNewGame); //associa alla rotta per creare una nuova partita il relativo metodo
		router.route(HttpMethod.POST, JOIN_GAME_RESOURCE_PATH).handler(this::joinGame); //associa alla rotta per far entrare un utente in una partita il relativo metodo
		router.route(HttpMethod.GET, GAME_RESOURCE_PATH).handler(this::getGameInfo); //associa alla rotta per recuperare le informazioni di una partita il relativo metodo
		router.route(HttpMethod.POST, PLAYER_MOVE_RESOURCE_PATH).handler(this::makeAMove); //associa alla rotta per eseguire una mossa il relativo metodo
		handleEventSubscription(server); //registra un websocket handler al server per ascoltare le richieste del client

		router.route("/public/*").handler(StaticHandler.create()); //gestisce le richieste del client che iniziano con "public", relative all'aspetto della pagina web

		var fut = server.requestHandler(router).listen(port); //avvia il server sulla porta specificata
		fut.onSuccess(res -> { //in caso di avvio con successo
			logger.log(Level.INFO, "TTT API Gateway ready - port: " + port); //stampa un messaggio di log
		});

		return fut; //restituisce la future
	}

    //crea un account utente
    protected void createNewAccount(RoutingContext context) {
		logger.log(Level.INFO, "create a new account");
		context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
			logger.log(Level.INFO, "Payload: " + userInfo);
			var userName = userInfo.getString("userName"); //estrae il valore del campo "userName"
			var password = userInfo.getString("password"); //estrae il valore del campo "password"
			var reply = new JsonObject(); //crea un oggetto json di risposta al client
			/*
			dato che si sta utilizzando vertx, si utilizza un event loop (in realtà uno per ogni core),
			e dato le operazioni richieste sono operazioni bloccanti (poiché coinvolgono chiamate http) è necessario delegare il compito di esecuzione a un worker thread
			 */
			this.vertx.executeBlocking(() -> { //crea un worker thread
				return accountService.registerUser(userName, password);	//registra un nuovo utente
			}).onSuccess((ref) -> { //se l'operazione è completata con successo (il parametro è una stringa, non è un oggetto)
				reply.put("result", "ok"); //popola l'oggetto con un informazion di successo
				var loginPath = LOGIN_RESOURCE_PATH.replace(":accountId", userName); //costruisce il link per il login
				reply.put("loginLink", loginPath); //popola l'oggetto con il link per il login
				reply.put("accountLink", ref.accountRefLink());	//popola l'oggetto con il link alle informazioni dell'account
				sendReply(context.response(), reply); //invia la risposta al client
			}).onFailure((f) -> { //altrimenti
				reply.put("result", "error"); //popola l'oggetto con un informazione di errore
				reply.put("error", f.getMessage()); //popola l'oggetto con la specifica dell'errore
				sendReply(context.response(), reply); //invia la risposta al client
			});
		});
	}

    //recupera le informazioni di un account
	protected void getAccountInfo(RoutingContext context) {
		logger.log(Level.INFO, "get account info");
		var userName = context.pathParam("accountId"); //estrae (dall'url) il valore del campo "userName"
		var reply = new JsonObject(); //crea un oggetto json di risposta al client
		this.vertx.executeBlocking(() -> { //crea un worker thread
			var acc = accountService.getAccountInfo(userName); //recupera l'account associato a "userName"
			return acc; //restituisce l'account
		}).onSuccess((res) -> { //se l'operazione è completata con successo
			reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
			var accJson = new JsonObject(); //crea un oggetto json account per memorizzare le informazioni dell'account
			accJson.put("userName", res.userName()); //popola l'oggetto account con il relativo username
			accJson.put("password", res.password()); //popola l'oggetto account con la relativa password
			accJson.put("whenCreated", res.whenCreated()); //popola l'oggetto account con la relativa data di creazione
			reply.put("accountInfo", accJson); //popola l'oggetto di risposta con l'oggetto account
			sendReply(context.response(), reply); //invia la risposta al client
		}).onFailure((f) -> { //altrimenti
			reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
			reply.put("error", "account-not-present"); //popola l'oggetto con la specifica dell'errore
			sendReply(context.response(), reply); //invia la risposta al client
		});
	}

    //logga un utente
	protected void login(RoutingContext context) {
		logger.log(Level.INFO, "Login request");
		context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
			JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
			logger.log(Level.INFO, "Payload: " + userInfo);
			var userName = userInfo.getString("userName"); //estrae il valore del campo "userName"
			var password = userInfo.getString("password"); //estrae il valore del campo "password"
			var reply = new JsonObject(); //crea un oggetto json di risposta al client
			this.vertx.executeBlocking(() -> { //crea un worker thread
				var sessionId = lobbyService.login(userName, password); //esegue il login dell'utente
				return sessionId; //restituisce l'oggetto rappresentativo della sessione utente creata
			}).onSuccess((sessionId) -> { //se l'operazione è completata con successo
				reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
				var createPath = CREATE_GAME_RESOURCE_PATH.replace(":sessionId", sessionId); //costruisce il link per la creazione della partita
				var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", sessionId); //costruisce il link per entrare nella partita
				reply.put("createGameLink", createPath); //popola l'oggetto con il link per la creazione della partita
				reply.put("joinGameLink", joinPath); //popola l'oggetto con il link per il login
				reply.put("sessionId", sessionId); //popola l'oggetto con l'id della sessione
				reply.put("sessionLink", USER_SESSIONS_RESOURCE_PATH + "/" + sessionId); //popola l'oggetto con il link della sessione
				sendReply(context.response(), reply); //invia la risposta al client
			}).onFailure((f) -> { //altrimenti
				reply.put("result", "login-failed"); //popola l'oggetto con un'informazione di errore
				reply.put("error", f.getMessage()); //popola l'oggetto con la specifica dell'errore
				sendReply(context.response(), reply); //invia la risposta al client
			});			
		});
	}

    //crea una nuova partita
	protected void createNewGame(RoutingContext context) {
		logger.log(Level.INFO, "CreateNewGame request - " + context.currentRoute().getPath());
		context.request().handler(buf -> {  //prende il body del messaggio http inviato dal client
			JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
			var sessionId = context.pathParam("sessionId"); //estrae (dall'url) il valore del campo "sessionId"
			var gameId = userInfo.getString("gameId"); //estrae (dall'url) il valore del campo "gameId"
			var reply = new JsonObject(); //crea un oggetto json di risposta al client
			vertx.executeBlocking(() -> { //crea unu worker thread
				lobbyService.createNewGame(sessionId, gameId); //crea una partita
				return null; //termina
			}).onSuccess((res) -> { //se l'operazione è completata con successo
				reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
				reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId); //popola l'oggetto con il link alla partita creata
				var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", sessionId); //costruisce il link per entrare nella partita
				reply.put("joinGameLink", joinPath); //popola l'oggetto con il link per entrare nella partita
				sendReply(context.response(), reply); //invia la risposta al client
			}).onFailure((f) -> { //altrimenti
				reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
				reply.put("error", "game-already-present"); //popola l'oggetto con la specifica dell'errore
				sendReply(context.response(), reply); //invia la risposta al client
			});
		});		
	}

    //consente a un utente di unirsi a una partita
	protected void joinGame(RoutingContext context) {
		logger.log(Level.INFO, "JoinGame request - " + context.currentRoute().getPath());
		context.request().handler(buf -> {  //prende il body del messaggio http inviato dal client
			JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
			var sessionId = context.pathParam("sessionId"); //estrae (dall'url) il valore del campo "sessionId"
			var gameId = userInfo.getString("gameId"); //estrae il valore del campo "gameId"
			var symbol = userInfo.getString("symbol"); //estrae il valore del campo "symbol"
			var reply = new JsonObject(); //crea un oggetto json di risposta al client
			vertx.executeBlocking(() -> { //crea un worker thread
				var playerSessionId = lobbyService.joinGame(sessionId, gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O); //esegue il join dell'utente nella partita
				return playerSessionId; //restituisce l'oggetto rappresentativo della sessione giocatore creata
			}).onSuccess((playerSessionId) -> { //se l'operazione è completata con successo
				reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
				reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId); //popola l'oggetto con il link alla partita
				sendReply(context.response(), reply); //invia la risposta al client
			}).onFailure((f) -> { //altrimenti
				reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
				reply.put("error", "game-already-present"); //popola l'oggetto con la specifica dell'errore
				sendReply(context.response(), reply); //invia la risposta al client
				sendError(context.response()); //invia un errore al client
			});
		});		
	}

    //recupera le informazioni di una partita
	protected void getGameInfo(RoutingContext context) {
		logger.log(Level.INFO, "get game info");
		var gameId = context.pathParam("gameId"); //estrae (dall'url) il valore del campo "gameId"
		var reply = new JsonObject(); //crea un oggetto json di risposta al client
		this.vertx.executeBlocking(() -> { //crea un worker thread
			var game = gameService.getGameInfo(gameId); //recupera la partita con id "gameId"
			return game; //restituisce la partita creata
		}).onSuccess((game) -> { //se l'operazione è completata con successo
			reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
			var gameJson = new JsonObject(); //crea un oggetto json partita per memorizzare le informazioni della partita
			gameJson.put("gameId", game.gameId()); //popola l'oggetto partita con il relativo id
			var st = game.gameState(); //recupera lo stato della partita
			gameJson.put("gameState", st); //popola l'oggetto partita con il relativo stato
			if (st.equals("started")  || st.equals("finished")) { //se la partita è iniziata o terminata
				var bs = game.boardState(); //recupera lo stato della griglia
				JsonArray array = new JsonArray(); //crea un arrray per lo stato della griglia
				for (var el: bs) { //per ogni elemento della griglia
					array.add(el); //lo aggiunge all'array
				}
				gameJson.put("boardState", array); //popola l'oggetto partita con l'array
			}
			if (st.equals("started")) { //se la partita è iniziata
				gameJson.put("turn", game.currentTurn()); //popola l'oggetto partita con il turno corrente
			}			
			reply.put("gameInfo", gameJson); //popola l'oggetto di risposta con l'oggetto partita
			sendReply(context.response(), reply); //invia la risposta al client
		}).onFailure((f) -> { //altrimenti
			reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
			reply.put("error", "game-not-present"); //popola l'oggetto con la specifica dell'errore
			sendReply(context.response(), reply); //invia la risposta al client
		});
	}


    //esegue una mossa
	protected void makeAMove(RoutingContext context) {
		logger.log(Level.INFO, "MakeAMove request - " + context.currentRoute().getPath());
		context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
			var reply = new JsonObject(); //crea un oggetto json di risposta al client
			try {
				JsonObject moveInfo = buf.toJsonObject(); //converte il body in un oggetto json
				logger.log(Level.INFO, "move info: " + moveInfo);
				var gameId = context.pathParam("gameId"); //estrae (dall'url) il valore del campo "gameId"
				var sessionId = context.pathParam("playerSessionId"); //estrae il valore del campo "playerSessionId"
				int x = Integer.parseInt(moveInfo.getString("x")); //estrae il valore del campo "x" e lo converte in un intero
				int y = Integer.parseInt(moveInfo.getString("y")); //estrae il valore del campo "y" e lo converte in un intero
				vertx.executeBlocking(() -> { //crea un worker thread
					gameService.makeAMove(gameId, sessionId, x, y); //fa eseguire al giocatore una mossa
					return null; //termina
				}).onSuccess((r) -> { //se l'operazione è completata con successo
					reply.put("result", "accepted"); //popola l'oggetto con un'informazione di successo
					var movePath = PLAYER_MOVE_RESOURCE_PATH.replace(":gameId", gameId).replace(":playerSessionId", sessionId); //costruisce il link per eseguire una mossa nella partita
					reply.put("moveLink", movePath); //popola l'oggetto con il link per eseguire la mossa
					reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId); //popola l'oggetto con il link alla partita
					sendReply(context.response(), reply); //invia la risposta al client
				}).onFailure((f) -> { //altrimenti
					reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
					reply.put("error", "invalid-move"); //popola l'oggetto con la specifica dell'errore
					sendReply(context.response(), reply); //invia la risposta al client
				});
			} catch (Exception ex1) {
				reply.put("result", "error");
				reply.put("error", ex1.getMessage());
                sendReply(context.response(), reply);
                sendError(context.response());
			}
		});
	}

    //registra un websocket handler al server
	protected void handleEventSubscription(HttpServer server) {
		server.webSocketHandler(webSocket -> { //registra un handler per websocket
			logger.log(Level.INFO, "New TTT subscription accepted.");
			webSocket.textMessageHandler(openMsg -> { //imposta un handler per i messaggi ricevuti dal client
				logger.log(Level.INFO, "For game: " + openMsg);
				JsonObject obj = new JsonObject(openMsg); //converte il messaggio ricevuto dal client in un oggetto json
				String playerSessionId = obj.getString("playerSessionId"); //estrae il valore del campo "playerSessionId"
                EventBus eb = vertx.eventBus(); //recupera l'event bus di vertx
				gameService.createAnEventChannel(playerSessionId, vertx); //crea un nuovo canale per ricevere gli eventi di gioco
				eb.consumer(playerSessionId, msg -> { //iscrive l'event bus alla sessione giocatore e, ogni volta che arriva un messaggio all'event bus...
					webSocket.writeTextMessage(msg.body().toString()); //lo invia al client tramite websocket
				});
			});
		});
	}

    //invia la risposta al client
    private void sendReply(HttpServerResponse response, JsonObject reply) {
        response.putHeader("content-type", "application/json"); //imposta l’header del messaggio http come json
        response.end(reply.toString()); //converte l’oggetto json in stringa, lo invia al client e chiude la risposta
    }

    //invia una risposta di errore al client
    private void sendError(HttpServerResponse response) {
        response.setStatusCode(500);  //imposta lo stato della risposta a 500 (errore)
        response.putHeader("content-type", "application/json"); //imposta l’header del messaggio http come json
        response.end(); //chiude la risposta
    }
}
