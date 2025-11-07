package ttt_game_service.infrastructure;

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
import ttt_game_service.application.*;
import ttt_game_service.domain.*;

/**
*
* Game Service controller
* 
* @author aricci
*
*/
public class GameServiceController extends VerticleBase  {

	private int port;
	static Logger logger = Logger.getLogger("[Game Service Controller]");

	static final String API_VERSION = "v1";
	static final String GAMES_RESOURCE_PATH = "/api/" + API_VERSION + "/games";
	static final String GAME_RESOURCE_PATH =  GAMES_RESOURCE_PATH +   "/:gameId";
	static final String JOIN_GAME_RESOURCE_PATH = GAME_RESOURCE_PATH + "/join";
	static final String PLAYER_MOVE_RESOURCE_PATH = GAME_RESOURCE_PATH + "/:playerSessionId/move";
	static final String GAME_EVENT_CHANNEL_PATH = "/api/" + API_VERSION +"/events"; 
	
	/* Ref. to the application layer */
	private GameService gameService;
	
	public GameServiceController(GameService service, int port) {
		this.port = port;
		logger.setLevel(Level.INFO);
		this.gameService = service;

	}

	public Future<?> start() {
		logger.log(Level.INFO, "TTT Game Service initializing...");
		HttpServer server = vertx.createHttpServer();
				
		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, GAMES_RESOURCE_PATH).handler(this::createNewGame);
		router.route(HttpMethod.GET, GAME_RESOURCE_PATH).handler(this::getGameInfo);
		router.route(HttpMethod.POST, JOIN_GAME_RESOURCE_PATH).handler(this::joinGame);
		router.route(HttpMethod.POST, PLAYER_MOVE_RESOURCE_PATH).handler(this::makeAMove);
		this.handleEventSubscription(server, GAME_EVENT_CHANNEL_PATH);

		/* static files */
		
		router.route("/public/*").handler(StaticHandler.create());
		
		/* start the server */
		
		var fut = server
			.requestHandler(router)
			.listen(port);
		
		fut.onSuccess(res -> {
			logger.log(Level.INFO, "TTT Game Service ready - port: " + port);
		});

		return fut;
	}


	/* List of handlers mapping the API */
	

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
			logger.log(Level.INFO, "Payload: " + userInfo);
			var reply = new JsonObject();
			try {
				var gameId = userInfo.getString("gameId");
				this.gameService.createNewGame(gameId);
				reply.put("result", "ok");
				reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId);
				var joinPath = GAMES_RESOURCE_PATH + "/" + gameId + "/join";
				reply.put("joinGameLink", joinPath);
				sendReply(context.response(), reply);
			} catch (GameAlreadyPresentException ex) {
				reply.put("result", "error");
				reply.put("error", "game-already-present");
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}			
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
			try {
				var game = gameService.getGameInfo(gameId);
				reply.put("result", "ok");
				var gameJson = new JsonObject();
				gameJson.put("gameId", game.getId());
				gameJson.put("gameState", game.getGameState());
				if (game.isStarted() || game.isFinished()) {
					var bs = game.getBoardState();
					JsonArray array = new JsonArray();
					for (var el: bs) {
						array.add(el);
					}
					gameJson.put("boardState", array);
				}
				if (game.isStarted()) {
					gameJson.put("turn", game.getCurrentTurn());
				}
				reply.put("gameInfo", gameJson);			
				sendReply(context.response(), reply);
			} catch (GameNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", "game-not-present");
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
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
			JsonObject joinInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Join info: " + joinInfo);
			
			String gameId = context.pathParam("gameId");
			String userId = joinInfo.getString("userId");
			String symbol = joinInfo.getString("symbol");

			var reply = new JsonObject();
			try {
				var playerSession = gameService.joinGame(new UserId(userId), 
												gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O, 
												new VertxPlayerSessionEventObserver(vertx.eventBus()));
				reply.put("playerSessionId", playerSession.getId());
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (InvalidJoinException  ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}			
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
			var  reply = new JsonObject();
			try {
				JsonObject moveInfo = buf.toJsonObject();
				logger.log(Level.INFO, "move info: " + moveInfo);				
				String playerSessionId = context.pathParam("playerSessionId");
				int x = Integer.parseInt(moveInfo.getString("x"));
				int y = Integer.parseInt(moveInfo.getString("y"));
				var ps = gameService.getPlayerSession(playerSessionId);
				ps.makeMove(x, y);				
				reply.put("result", "accepted");
				var gameId = context.pathParam("gameId");
				var movePath = PLAYER_MOVE_RESOURCE_PATH
						.replace(":gameId",gameId)
						.replace(":playerSessionId",ps.getId());
				reply.put("moveLink", movePath);
				reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId);
				sendReply(context.response(), reply);
			} catch (InvalidMoveException ex) {
				reply.put("result", "invalid-move");
				sendReply(context.response(), reply);				
			} catch (Exception ex1) {
				reply.put("result", ex1.getMessage());
				try {
					sendReply(context.response(), reply);
				} catch (Exception ex2) {
					sendError(context.response());
				}				
			}
		});
	}


	/* Handling subscribers using web sockets */
	
	protected void handleEventSubscription(HttpServer server, String path) {
		server.webSocketHandler(webSocket -> {
			logger.log(Level.INFO, "New TTT subscription accepted.");

			/* 
			 * 
			 * Receiving a first message including the id of the game
			 * to observe 
			 * 
			 */
			webSocket.textMessageHandler(openMsg -> {
				logger.log(Level.INFO, "For game: " + openMsg);
				JsonObject obj = new JsonObject(openMsg);
				String playerSessionId = obj.getString("playerSessionId");
				
				
				/* 
				 * Subscribing events on the event bus to receive
				 * events concerning the game, to be notified 
				 * to the frontend using the websocket
				 * 
				 */
				EventBus eb = vertx.eventBus();
				
				eb.consumer(playerSessionId, msg -> {
					JsonObject ev = (JsonObject) msg.body();
					logger.log(Level.INFO, "Event: " + ev.encodePrettily());
					webSocket.writeTextMessage(ev.encodePrettily());
				});
				
				var ps = gameService.getPlayerSession(playerSessionId);
				var en = ps.getPlayerSessionEventNotifier();
				en.enableEventNotification(playerSessionId);
								
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
