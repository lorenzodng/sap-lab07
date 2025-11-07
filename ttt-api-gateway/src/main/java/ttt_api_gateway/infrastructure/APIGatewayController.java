package ttt_api_gateway.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.StaticHandler;
import ttt_api_gateway.application.*;

/**
*
* API Gateway Controller
* 
*/
public class APIGatewayController extends VerticleBase  {

	private int port;
	static Logger logger = Logger.getLogger("[APIGatewayController]");

	/* for account */
	static final String API_VERSION = "v1";
	static final String ACCOUNTS_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts";
	static final String ACCOUNT_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId";
	static final String CHECK_PWD_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId/check-pwd";
	
	/* for lobby */
	static final String LOGIN_RESOURCE_PATH = 			"/api/" + API_VERSION + "/lobby/login";
	static final String USER_SESSIONS_RESOURCE_PATH = 	"/api/" + API_VERSION + "/lobby/user-sessions";
	static final String CREATE_GAME_RESOURCE_PATH = 	"/api/" + API_VERSION + "/lobby/user-sessions/:sessionId/create-game";
	static final String JOIN_GAME_RESOURCE_PATH = 		"/api/" + API_VERSION + "/lobby/user-sessions/:sessionId/join-game";

	/* for game */
	static final String GAMES_RESOURCE_PATH = "/api/" + API_VERSION + "/games";
	static final String GAME_RESOURCE_PATH =  GAMES_RESOURCE_PATH +   "/:gameId";
	static final String PLAYER_MOVE_RESOURCE_PATH = GAME_RESOURCE_PATH + "/:playerSessionId/move";
	static final String WS_EVENT_CHANNEL_PATH = "/api/" + API_VERSION + "/events";
	
	/* proxies to interact with the services */
	
	private GameService gameService;
	private AccountService accountService;
	private LobbyService lobbyService;
	
	
	public APIGatewayController(AccountService accountService, LobbyService lobbyService, GameService gameService, int port) {
		this.port = port;
		logger.setLevel(Level.INFO);
		this.gameService = gameService;
		this.accountService = accountService;
		this.lobbyService = lobbyService;

	}

	public Future<?> start() {
		logger.log(Level.INFO, "TTT Game Service initializing...");
		HttpServer server = vertx.createHttpServer();

		/* REST API routes */
				
		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, ACCOUNTS_RESOURCE_PATH).handler(this::createNewAccount);
		router.route(HttpMethod.GET, ACCOUNT_RESOURCE_PATH).handler(this::getAccountInfo);
		router.route(HttpMethod.POST, LOGIN_RESOURCE_PATH).handler(this::login);
		router.route(HttpMethod.POST, CREATE_GAME_RESOURCE_PATH).handler(this::createNewGame);
		router.route(HttpMethod.POST, JOIN_GAME_RESOURCE_PATH).handler(this::joinGame);
		
		router.route(HttpMethod.GET, GAME_RESOURCE_PATH).handler(this::getGameInfo);
		router.route(HttpMethod.POST, PLAYER_MOVE_RESOURCE_PATH).handler(this::makeAMove);
		handleEventSubscription(server, WS_EVENT_CHANNEL_PATH);

		/* static files */
		
		router.route("/public/*").handler(StaticHandler.create());
		
		/* start the server */
		
		var fut = server
			.requestHandler(router)
			.listen(port);
		
		fut.onSuccess(res -> {
			logger.log(Level.INFO, "TTT API Gateway ready - port: " + port);
		});

		return fut;
	}


	/* List of handlers mapping the API */
	
	/**
	 * 
	 * Register a new user
	 * 
	 * @param context
	 */
	protected void createNewAccount(RoutingContext context) {
		logger.log(Level.INFO, "create a new account");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + userInfo);
			var userName = userInfo.getString("userName");
			var password = userInfo.getString("password");

			var reply = new JsonObject();
			
			/* 
			 * we cannot block the event loop, so - since proxies are synchronous,
			 * we need to delegate the call to a background thread
			 */
			this.vertx.executeBlocking(() -> {
				return  accountService.registerUser(userName, password);	
			}).onSuccess((ref) -> {
				reply.put("result", "ok");
				var loginPath = LOGIN_RESOURCE_PATH.replace(":accountId", userName);
				reply.put("loginLink", loginPath);
				reply.put("accountLink", ref.accountRefLink());				
				sendReply(context.response(), reply);
			}).onFailure((f) -> {
				reply.put("result", "error");
				reply.put("error", f.getMessage());
				sendReply(context.response(), reply);
			});
		});
	}

	/**
	 * 
	 * Get account info
	 * 
	 * @param context
	 */
	protected void getAccountInfo(RoutingContext context) {
		logger.log(Level.INFO, "get account info");
		var userName = context.pathParam("accountId");
		var reply = new JsonObject();
		this.vertx.executeBlocking(() -> {
			var acc = accountService.getAccountInfo(userName);	
			return acc;
		}).onSuccess((res) -> {
			reply.put("result", "ok");
			var accJson = new JsonObject();
			accJson.put("userName", res.userName());
			accJson.put("password", res.password());
			accJson.put("whenCreated", res.whenCreated());
			reply.put("accountInfo", accJson);			
			sendReply(context.response(), reply);
		}).onFailure((f) -> {
			reply.put("result", "error");
			reply.put("error", "account-not-present");
			sendReply(context.response(), reply);
		});
	}
	
	/**
	 * 
	 * Login a user
	 * 
	 * It creates a User Session
	 * 
	 * @param context
	 */
	protected void login(RoutingContext context) {
		logger.log(Level.INFO, "Login request");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + userInfo);
			var userName = userInfo.getString("userName");
			var password = userInfo.getString("password");
			var reply = new JsonObject();
			this.vertx.executeBlocking(() -> {
				var sessionId = lobbyService.login(userName, password);
				return sessionId;
			}).onSuccess((sessionId) -> {
				reply.put("result", "ok");
				var createPath = CREATE_GAME_RESOURCE_PATH.replace(":sessionId", sessionId);
				var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", sessionId);
				reply.put("createGameLink", createPath);
				reply.put("joinGameLink", joinPath);
				reply.put("sessionId", sessionId);
				reply.put("sessionLink", USER_SESSIONS_RESOURCE_PATH + "/" + sessionId);				
				sendReply(context.response(), reply);
			}).onFailure((f) -> {
				reply.put("result", "login-failed");
				reply.put("error", f.getMessage());
				sendReply(context.response(), reply);
			});			
		});
	}
	
	
	/**
	 * 
	 * Create a New Game - by users logged in (with a UserSession)
	 * 
	 * @param context
	 */
	protected void createNewGame(RoutingContext context) {
		logger.log(Level.INFO, "CreateNewGame request - " + context.currentRoute().getPath());
		context.request().handler(buf -> { 
			JsonObject userInfo = buf.toJsonObject();
			var sessionId = context.pathParam("sessionId");
			var gameId = userInfo.getString("gameId");
			var reply = new JsonObject();
			vertx.executeBlocking(() -> {
				lobbyService.createNewGame(sessionId, gameId);
				return null;
			}).onSuccess((res) -> {
				reply.put("result", "ok");
				reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId);
				var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", sessionId);
				reply.put("joinGameLink", joinPath);
				sendReply(context.response(), reply);
			}).onFailure((f) -> {
				reply.put("result", "error");
				reply.put("error", "game-already-present");
				sendReply(context.response(), reply);
				sendError(context.response());
			});			
		});		
	}

	/**
	 * 
	 * Join a Game - by user logged in (with a UserSession)
	 * 
	 * It creates a PlayerSession
	 * 
	 * @param context
	 */
	protected void joinGame(RoutingContext context) {
		logger.log(Level.INFO, "JoinGame request - " + context.currentRoute().getPath());
		context.request().handler(buf -> { 
			JsonObject userInfo = buf.toJsonObject();
			var sessionId = context.pathParam("sessionId");
			var gameId = userInfo.getString("gameId");
			var symbol = userInfo.getString("symbol");
			var reply = new JsonObject();
			vertx.executeBlocking(() -> {
				var playerSessionId = lobbyService.joinGame(sessionId, gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O);
				return playerSessionId;
			}).onSuccess((playerSessionId) -> {
				reply.put("result", "ok");
				reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId);
				var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", playerSessionId);
				reply.put("joinGameLink", joinPath);
				sendReply(context.response(), reply);
			}).onFailure((f) -> {
				reply.put("result", "error");
				reply.put("error", "game-already-present");
				sendReply(context.response(), reply);
				sendError(context.response());
			});			
		});		
	}	
	
	/**
	 * 
	 * Get game info
	 * 
	 * @param context
	 */
	protected void getGameInfo(RoutingContext context) {
		logger.log(Level.INFO, "get game info");
		var gameId = context.pathParam("gameId");
		var reply = new JsonObject();
		this.vertx.executeBlocking(() -> {
			var game = gameService.getGameInfo(gameId);
			return game;
		}).onSuccess((game) -> {
			reply.put("result", "ok");
			var gameJson = new JsonObject();
			gameJson.put("gameId", game.gameId());
			var st = game.gameState();
			gameJson.put("gameState", st);
			
			if (st.equals("started")  || st.equals("finished")) {
				var bs = game.boardState();
				JsonArray array = new JsonArray();
				for (var el: bs) {
					array.add(el);
				}
				gameJson.put("boardState", array);
			}
			if (st.equals("started")) {
				gameJson.put("turn", game.currentTurn());
			}			
			reply.put("gameInfo", gameJson);			
			sendReply(context.response(), reply);
		}).onFailure((f) -> {
			reply.put("result", "error");
			reply.put("error", "game-not-present");
			sendReply(context.response(), reply);
		});
	}
	

	
	/**
	 * 
	 * Make a move in a game - by players playing a game (with a PlayerSession)
	 * 
	 * @param context
	 */
	protected void makeAMove(RoutingContext context) {
		logger.log(Level.INFO, "MakeAMove request - " + context.currentRoute().getPath());
		context.request().handler(buf -> {
			var reply = new JsonObject();
			try {
				JsonObject moveInfo = buf.toJsonObject();
				logger.log(Level.INFO, "move info: " + moveInfo);
				var gameId = context.pathParam("gameId");
				var sessionId = context.pathParam("playerSessionId");
				int x = Integer.parseInt(moveInfo.getString("x"));
				int y = Integer.parseInt(moveInfo.getString("y"));
				vertx.executeBlocking(() -> {
					gameService.makeAMove(gameId, sessionId, x, y);
					return null;
				}).onSuccess((r) -> {
					reply.put("result", "accepted");
					var movePath = PLAYER_MOVE_RESOURCE_PATH.replace(":gameId", gameId).replace(":playerSessionId",
							sessionId);
					reply.put("moveLink", movePath);
					reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId);
					sendReply(context.response(), reply);
				}).onFailure((f) -> {
					reply.put("result", "error");
					reply.put("error", "invalid-move");
					sendReply(context.response(), reply);
				});
			} catch (Exception ex1) {
				reply.put("result", "error");
				reply.put("error", ex1.getMessage());
				try {
					sendReply(context.response(), reply);
				} catch (Exception ex2) {
					sendError(context.response());
				}
			}
		});
	}

	/**
	 * 
	 * Handling subscribers using web sockets
	 * 
	 * @param server
	 * @param path
	 */
	protected void handleEventSubscription(HttpServer server, String path) {
		server.webSocketHandler(webSocket -> {
			logger.log(Level.INFO, "New TTT subscription accepted.");

			webSocket.textMessageHandler(openMsg -> {
				logger.log(Level.INFO, "For game: " + openMsg);
				JsonObject obj = new JsonObject(openMsg);
				String playerSessionId = obj.getString("playerSessionId");
				
				/* create a channel to make the bridge */
				
				var eb = vertx.eventBus();
				
				gameService.createAnEventChannel(playerSessionId, vertx);

				eb.consumer(playerSessionId, msg -> {
					/* bridge between web sockets */
					webSocket.writeTextMessage(msg.body().toString());
				});
						
			});
		});
	}
	
	/* Aux methods */
	

	private void sendReply(HttpServerResponse response, JsonObject reply) {
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}
	
	private void sendError(HttpServerResponse response) {
		response.setStatusCode(500);
		response.putHeader("content-type", "application/json");
		response.end();
	}


}
