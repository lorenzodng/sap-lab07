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

/*
controller di backend (intermediario client <-> servizio di gioco):
client -> controller che utilizza un server -> servizio di gioco
 */
public class GameServiceController extends VerticleBase  {

    private int port; //porta su cui il server ascolta le richieste http
    static Logger logger = Logger.getLogger("[Game Service Controller]");
    static final String API_VERSION = "v1"; //versione dell'API utilizzata per definire le rotte
    static final String GAMES_RESOURCE_PATH = "/api/" + API_VERSION + "/games"; //rotta per creare una partita
    static final String GAME_RESOURCE_PATH =  GAMES_RESOURCE_PATH +   "/:gameId"; //rotta per recuperare le informazioni di una partita
    static final String JOIN_GAME_RESOURCE_PATH = GAME_RESOURCE_PATH + "/join"; //rotta per far entrare un utente in una partita
    static final String PLAYER_MOVE_RESOURCE_PATH = GAME_RESOURCE_PATH + "/:playerSessionId/move"; //rotta per eseguire una mossa
    private GameService gameService; //servizio di gioco

    public GameServiceController(GameService service, int port) {
        this.port = port;
        logger.setLevel(Level.INFO);
        this.gameService = service;

    }

    //avvia il server (eseguito automaticamente alla chiamata "vertx.deployVerticle(server)")
    public Future<?> start() {
        logger.log(Level.INFO, "TTT Game Service initializing...");
        HttpServer server = vertx.createHttpServer(); //crea un sever http

        Router router = Router.router(vertx); //router per l'instradamento delle richieste http
        router.route(HttpMethod.POST, GAMES_RESOURCE_PATH).handler(this::createNewGame); //associa alla rotta per creare una nuova partita il relativo metodo
        router.route(HttpMethod.GET, GAME_RESOURCE_PATH).handler(this::getGameInfo); //associa alla rotta per recuperare le informazioni di una partita il relativo metodo
        router.route(HttpMethod.POST, JOIN_GAME_RESOURCE_PATH).handler(this::joinGame); //associa alla rotta per far entrare un utente in una partita il relativo metodo
        router.route(HttpMethod.POST, PLAYER_MOVE_RESOURCE_PATH).handler(this::makeAMove); //associa alla rotta per eseguire una mossa il relativo metodo
        this.handleEventSubscription(server); //registra un websocket handler al server per ascoltare le richieste del client

        router.route("/public/*").handler(StaticHandler.create()); //gestisce le richieste del client che iniziano con "public", relative all'aspetto della pagina web

        var fut = server.requestHandler(router).listen(port); //avvia il server sulla porta specificata
        fut.onSuccess(res -> { //in caso di avvio con successo
            logger.log(Level.INFO, "TTT Game Service ready - port: " + port); //stampa un messaggio di log
        });

        return fut; //restituisce la future
    }

    //crea una nuova partita
    protected void createNewGame(RoutingContext context) { //context è l'oggetto che rappresenta la richiesta http
        logger.log(Level.INFO, "CreateNewGame request - " + context.currentRoute().getPath());
        context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
            logger.log(Level.INFO, "Payload: " + userInfo);
            var reply = new JsonObject(); //crea un oggetto json di risposta al client
            try {
                var gameId = userInfo.getString("gameId"); //estrae il valore del campo "gameId"
                this.gameService.createNewGame(gameId); //crea una partita
                reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
                reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId); //popola l'oggetto con il link della partita creata
                var joinPath = GAMES_RESOURCE_PATH + "/" + gameId + "/join"; //costruisce il link per entrare nella partita
                reply.put("joinGameLink", joinPath); //popola l'oggetto con il link per entrare nella partita
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (GameAlreadyPresentException ex) {
                reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
                reply.put("error", "game-already-present"); //popola l'oggetto con la specifica dell'errore
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (Exception ex1) {
                sendError(context.response()); //invia un errore al client
            }
        });
    }

    //recupera le informazioni di una partita
    protected void getGameInfo(RoutingContext context) {
        logger.log(Level.INFO, "get game info");
        var gameId = context.pathParam("gameId"); //estrae (dall'url) il valore del campo "gameId"
        var reply = new JsonObject(); //crea un oggetto json di risposta al client
        try {
            var game = gameService.getGameInfo(gameId); //recupera la partita con id "gameId"
            reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
            var gameJson = new JsonObject(); //crea un oggetto json partita per memorizzare le informazioni della partita
            gameJson.put("gameId", game.getId()); //popola l'oggetto partita con il relativo id
            gameJson.put("gameState", game.getGameState()); //popola l'oggetto partita con il relativo stato
            if (game.isStarted() || game.isFinished()) { //se la partita è iniziata o terminata
                var bs = game.getBoardState(); //recupera lo stato della griglia
                JsonArray array = new JsonArray(); //crea un arrray per lo stato della griglia
                for (var el : bs) { //per ogni elemento della griglia
                    array.add(el); //lo aggiunge all'array
                }
                gameJson.put("boardState", array); //popola l'oggetto partita con l'array
            }
            if (game.isStarted()) { //se la partita è iniziata
                gameJson.put("turn", game.getCurrentTurn()); //popola l'oggetto partita con il turno corrente
            }
            reply.put("gameInfo", gameJson); //popola l'oggetto di risposta con l'oggetto partita
            sendReply(context.response(), reply); //invia la risposta al client
        } catch (GameNotFoundException ex) {
            reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
            reply.put("error", "game-not-present"); //popola l'oggetto con la specifica dell'errore
            sendReply(context.response(), reply); //invia la risposta al client
        } catch (Exception ex1) {
            sendError(context.response()); //invia un errore al client
        }
    }

    //consente a un utente di unirsi a una partita
    protected void joinGame(RoutingContext context) {
        logger.log(Level.INFO, "JoinGame request - " + context.currentRoute().getPath());
        context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            JsonObject joinInfo = buf.toJsonObject(); //converte il body in un oggetto json
            logger.log(Level.INFO, "Join info: " + joinInfo);
            String gameId = context.pathParam("gameId"); //estrae (dall'url) il valore del campo "gameId"
            String userId = joinInfo.getString("userId"); //estrae il valore del campo "userId"
            String symbol = joinInfo.getString("symbol"); //estrae il valore del campo "symbol"
            var reply = new JsonObject(); //crea un oggetto json di risposta al client
            try {
                var playerSession = gameService.joinGame(new UserId(userId), gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O, new VertxPlayerSessionEventObserver(vertx.eventBus())); //esegue il join dell'utente nella partita
                reply.put("playerSessionId", playerSession.getId()); //popola l'oggetto con l'id della sessione giocatore
                reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (InvalidJoinException  ex) {
                reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
                reply.put("error", ex.getMessage()); //popola l'oggetto con la specifica dell'errore
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (Exception ex1) {
                sendError(context.response()); //invia un errore al client
            }
        });
    }

    //esegue una mossa
    protected void makeAMove(RoutingContext context) {
        logger.log(Level.INFO, "MakeAMove request - " + context.currentRoute().getPath());
        context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            var  reply = new JsonObject(); //crea un oggetto json di risposta al client
            try {
                JsonObject moveInfo = buf.toJsonObject(); //converte il body in un oggetto json
                logger.log(Level.INFO, "move info: " + moveInfo);
                String playerSessionId = context.pathParam("playerSessionId"); //converte il body in un oggetto json
                int x = Integer.parseInt(moveInfo.getString("x")); //estrae il valore del campo "x" e lo converte in un intero
                int y = Integer.parseInt(moveInfo.getString("y")); //estrae il valore del campo "y" e lo converte in un intero
                var ps = gameService.getPlayerSession(playerSessionId); //recupera la sessione del giocatore
                ps.makeMove(x, y); //fa eseguire al giocatore una mossa
                reply.put("result", "accepted"); //popola l'oggetto con un'informazione di successo
                var gameId = context.pathParam("gameId"); //estrae il valore del campo "gameId"
                var movePath = PLAYER_MOVE_RESOURCE_PATH.replace(":gameId",gameId).replace(":playerSessionId",ps.getId()); //costruisce il link per eseguire una mossa nella partita
                reply.put("moveLink", movePath); //popola l'oggetto con il link per eseguire la mossa
                reply.put("gameLink", GAMES_RESOURCE_PATH + "/" + gameId); //popola l'oggetto con il link alla partita
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (InvalidMoveException ex) {
                reply.put("result", "invalid-move"); //popola l'oggetto con un'informazione di errore
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (Exception ex1) {
                reply.put("result", ex1.getMessage()); //popola l'oggetto con la specifica dell'errore
                try {
                    sendReply(context.response(), reply); //invia la risposta al client
                } catch (Exception ex2) {
                    sendError(context.response()); //invia un errore al client
                }
            }
        });
    }

    //registra un websocket handler al server
    protected void handleEventSubscription(HttpServer server) {
        server.webSocketHandler(webSocket -> { //registra un handlder per websocket
            logger.log(Level.INFO, "New TTT subscription accepted.");
            webSocket.textMessageHandler(openMsg -> { //imposta un handler per i messaggi ricevuti dal client
                logger.log(Level.INFO, "For game: " + openMsg);
                JsonObject obj = new JsonObject(openMsg); //converte il messaggio ricevuto dal client in un oggetto json
                String playerSessionId = obj.getString("playerSessionId"); //estrae il valore del campo "playerSessionId"

                EventBus eb = vertx.eventBus(); //recupera l'event bus di vertx
                eb.consumer(playerSessionId, msg -> { //iscrive l'event bus all'indirizzo creato e, ogni volta che arriva un messaggio all'event bus...
                    JsonObject ev = (JsonObject) msg.body(); //...lo converte in json
                    logger.log(Level.INFO, "Event: " + ev.encodePrettily());
                    webSocket.writeTextMessage(ev.encodePrettily()); //lo invia al client tramite websocket
                });

                var ps = gameService.getPlayerSession(playerSessionId); //recupera la sessione del giocatore corrispondente al servizio principale (ovvero l'utente in prima persona)
                var en = ps.getPlayerSessionEventNotifier(); //recupera il notificatore di eventi della sessione del giocatore
                en.enableEventNotification(playerSessionId); //abilita la notifica degli eventi per questa sessione sul bus con l'indirizzo "playerSessionId"
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
