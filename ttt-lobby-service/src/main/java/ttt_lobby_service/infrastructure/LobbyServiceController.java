package ttt_lobby_service.infrastructure;

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
import ttt_lobby_service.application.LobbyService;
import ttt_lobby_service.application.LoginFailedException;
import ttt_lobby_service.domain.TTTSymbol;

/**
*
* Game Lobby Service controller
* * 
* @author aricci
*
*/
public class LobbyServiceController extends VerticleBase  {

	private int port;
	static Logger logger = Logger.getLogger("[LobbyController]");

	static final String API_VERSION = "v1";
	static final String LOGIN_RESOURCE_PATH = 			"/api/" + API_VERSION + "/lobby/login";
	static final String USER_SESSIONS_RESOURCE_PATH = 	"/api/" + API_VERSION + "/lobby/user-sessions";
	static final String CREATE_GAME_RESOURCE_PATH = 	"/api/" + API_VERSION + "/lobby/user-sessions/:sessionId/create-game";
	static final String JOIN_GAME_RESOURCE_PATH = 		"/api/" + API_VERSION + "/lobby/user-sessions/:sessionId/join-game";

	static final String GAME_SERVICE_PATH = 	"/api/v1/games";

	private String gameServiceAddress;
	private String gameServiceURI;
	
	/* Ref. to the application layer */
	private LobbyService lobbyService;
	
	public LobbyServiceController(LobbyService service, int port, String gameServiceAddress) {
		this.port = port;
		this.gameServiceAddress = gameServiceAddress;
		this.gameServiceURI = gameServiceAddress + GAME_SERVICE_PATH;
		logger.setLevel(Level.INFO);
		this.lobbyService = service;

	}

	public Future<?> start() {
		logger.log(Level.INFO, "TTT Lobby Service initializing...");
		HttpServer server = vertx.createHttpServer();

		/* REST API routes */
				
		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, LOGIN_RESOURCE_PATH).handler(this::login);
		router.route(HttpMethod.POST, CREATE_GAME_RESOURCE_PATH).handler(this::createGame);
		router.route(HttpMethod.POST, JOIN_GAME_RESOURCE_PATH).handler(this::joinGame);

		/* static files */
		
		router.route("/public/*").handler(StaticHandler.create());
		
		/* start the server */
		
		var fut = server
			.requestHandler(router)
			.listen(port);
		
		fut.onSuccess(res -> {
			logger.log(Level.INFO, "TTT Lobby Service ready - port: " + port);
		});

		return fut;
	}


	/* List of handlers mapping the API */
	
	
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
			try {
				var sessionId = lobbyService.login(userName, password);
				reply.put("result", "ok");
				var createPath = CREATE_GAME_RESOURCE_PATH.replace(":sessionId", sessionId);
				var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", sessionId);
				reply.put("sessionId", sessionId);
				reply.put("createGameLink", createPath);
				reply.put("joinGameLink", joinPath);
				reply.put("sessionLink", USER_SESSIONS_RESOURCE_PATH + "/" + sessionId);				
				sendReply(context.response(), reply);
			} catch (LoginFailedException ex) {
				reply.put("result", "login-failed");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}			
		});
	}

	/**
	 * 
	 * Create a game
	 * 
	 * @param context
	 */
	protected void createGame(RoutingContext context) {
		logger.log(Level.INFO, "Create game request");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			String sessionId = context.pathParam("sessionId");			
			var gameId = userInfo.getString("gameId");			
			var reply = new JsonObject();
			try {
				lobbyService.createNewGame(sessionId, gameId);
				reply.put("result", "ok");
				reply.put("gameLink", gameServiceURI + "/" + gameId);
				var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", sessionId);
				reply.put("joinGameLink", joinPath);
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} 			
		});
	}

	/**
	 * 
	 * Join a game
	 * 
	 * @param context
	 */
	protected void joinGame(RoutingContext context) {
		logger.log(Level.INFO, "Join game request");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			String sessionId = context.pathParam("sessionId");			
			var gameId = userInfo.getString("gameId");			
			var symbol = userInfo.getString("symbol");			
			var reply = new JsonObject();
			
			try {
				String playerSessionId = lobbyService.joinGame(sessionId, gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O);
				reply.put("result", "ok");
				reply.put("playerSessionId", playerSessionId);
				var movePath = gameServiceURI + "/" + gameId + "/" + playerSessionId + "/move";				
				reply.put("moveLink", movePath);
				reply.put("gameLink", gameServiceURI + "/" + gameId);
				reply.put("playerSessionLink", gameServiceURI + "/" + gameId + "/" + playerSessionId);
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				ex.printStackTrace();
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} 			
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
