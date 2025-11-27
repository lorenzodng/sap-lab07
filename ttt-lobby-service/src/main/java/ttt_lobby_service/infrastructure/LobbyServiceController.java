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

/*
controller di backend (intermediario client <-> servizio di gioco):
client -> controller che utilizza un server -> servizio di gioco
 */
public class LobbyServiceController extends VerticleBase  {

    private int port; //porta su cui il server ascolta le richieste http
    static Logger logger = Logger.getLogger("[LobbyController]");
    static final String API_VERSION = "v1"; //versione dell'API utilizzata per definire le rotte
    static final String LOGIN_RESOURCE_PATH = 			"/api/" + API_VERSION + "/lobby/login"; //rotta per il login dell'utente
    static final String USER_SESSIONS_RESOURCE_PATH = 	"/api/" + API_VERSION + "/lobby/user-sessions"; //rotta per recuperare le informazioni della sessione utente
    static final String CREATE_GAME_RESOURCE_PATH = 	"/api/" + API_VERSION + "/lobby/user-sessions/:sessionId/create-game"; //rotta per creare una partita
    static final String JOIN_GAME_RESOURCE_PATH = 		"/api/" + API_VERSION + "/lobby/user-sessions/:sessionId/join-game"; //rotta per entrare in una partita
    static final String GAME_SERVICE_URI = 	"http://localhost:9002/api/v1/games"; //il link della partita (gestita dal servizio di gestione del flusso di gioco)
    private LobbyService lobbyService; //servizio di lobby

    public LobbyServiceController(LobbyService service, int port) {
        this.port = port;
        logger.setLevel(Level.INFO);
        this.lobbyService = service;
    }

    //avvia il server (eseguito automaticamente alla chiamata "vertx.deployVerticle(server)")
    public Future<?> start() {
        logger.log(Level.INFO, "TTT Lobby Service initializing...");
        HttpServer server = vertx.createHttpServer(); //crea un sever http

        Router router = Router.router(vertx); //router per l'instradamento delle richieste http
        router.route(HttpMethod.POST, LOGIN_RESOURCE_PATH).handler(this::login); //associa alla rotta per il login il relativo metodo
        router.route(HttpMethod.POST, CREATE_GAME_RESOURCE_PATH).handler(this::createGame); //associa alla rotta per creare una partita il relativo metodo
        router.route(HttpMethod.POST, JOIN_GAME_RESOURCE_PATH).handler(this::joinGame); //associa alla rotta per entrare in una partita il relativo metodo

        router.route("/public/*").handler(StaticHandler.create()); //gestisce le richieste del client che iniziano con "public", relative all'aspetto della pagina web

        var fut = server.requestHandler(router).listen(port); //avvia il server sulla porta specificata
        fut.onSuccess(res -> { //in caso di avvio con successo
            logger.log(Level.INFO, "TTT Lobby Service ready - port: " + port); //stampa un messaggio di log
        });

        return fut; //restituisce la future
    }

    //esegue il login di un utente
    protected void login(RoutingContext context) {
        logger.log(Level.INFO, "Login request");
        context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
            logger.log(Level.INFO, "Payload: " + userInfo);
            var userName = userInfo.getString("userName"); //estrae il valore del campo "userName"
            var password = userInfo.getString("password"); //estrae il valore del campo "password"
            var reply = new JsonObject(); //crea un oggetto json di risposta al client
            try {
                var sessionId = lobbyService.login(userName, password); //esegue il login dell'utente
                reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
                var createPath = CREATE_GAME_RESOURCE_PATH.replace(":sessionId", sessionId); //costruisce il link per creare una partita
                var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", sessionId); //costruisce il link per entrare nella partita
                reply.put("sessionId", sessionId); //popola l'oggetto con l'id della sessione utente (creata dal login)
                reply.put("createGameLink", createPath); //popola l'oggetto con il link per creare una partita
                reply.put("joinGameLink", joinPath); //popola l'oggetto con il link per entrare nella partita
                reply.put("sessionLink", USER_SESSIONS_RESOURCE_PATH + "/" + sessionId); //popola l'oggetto con il link per recuperare le informazioni della sessione utente
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (LoginFailedException ex) {
                reply.put("result", "login-failed"); //popola l'oggetto con un'informazione di errore
                reply.put("error", ex.getMessage()); //popola l'oggetto con la specifica dell'errore
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (Exception ex1) {
                sendError(context.response()); //invia un errore al client
            }
        });
    }

    //crea una nuova partita
    protected void createGame(RoutingContext context) {
        logger.log(Level.INFO, "Create game request");
        context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
            String sessionId = context.pathParam("sessionId"); //estrae (dall'url) il valore del campo "sessionId"
            var gameId = userInfo.getString("gameId"); //estrae il valore del campo "gameId"
            var reply = new JsonObject(); //crea un oggetto json di risposta al client
            try {
                lobbyService.createNewGame(sessionId, gameId); //crea una partita
                var joinPath = JOIN_GAME_RESOURCE_PATH.replace(":sessionId", sessionId); //costruisce il link per entrare nella partita
                reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
                reply.put("gameLink", GAME_SERVICE_URI + "/" + gameId); //popola l'oggetto con il link della partita
                reply.put("joinGameLink", joinPath); //popola l'oggetto con il link per entrare nella partita
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (Exception ex) {
                reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
                reply.put("error", ex.getMessage()); //popola l'oggetto con la specifica dell'errore
                sendReply(context.response(), reply); //invia la risposta al client
            }
        });
    }

    //fa entrare un utente nella partita
    protected void joinGame(RoutingContext context) {
        logger.log(Level.INFO, "Join game request");
        context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
            String sessionId = context.pathParam("sessionId"); //estrae (dall'url) il valore del campo "sessionId"
            var gameId = userInfo.getString("gameId"); //estrae il valore del campo "userId"
            var symbol = userInfo.getString("symbol");	//estrae il valore del campo "symbol"
            var reply = new JsonObject(); //crea un oggetto json di risposta al client
            try {
                String playerSessionId = lobbyService.joinGame(sessionId, gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O); //esegue il join dell'utente nella partita
                reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
                reply.put("playerSessionId", playerSessionId); //popola l'oggetto con l'id della sessione giocatore
                var movePath = GAME_SERVICE_URI + "/" + gameId + "/" + playerSessionId + "/move"; //costruisce il link per eseguire la mossa di un giocatore (per capire come è costruito, guarda le rotte di "GameServiceController")
                reply.put("moveLink", movePath); //popola l'oggetto con il link per eseguire la mossa
                reply.put("gameLink", GAME_SERVICE_URI + "/" + gameId);  //popola l'oggetto con il link per recuperare le informazioni della partita
                reply.put("playerSessionLink", GAME_SERVICE_URI + "/" + gameId + "/" + playerSessionId);
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (Exception ex) {
                ex.printStackTrace();
                reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
                reply.put("error", ex.getMessage()); //popola l'oggetto con la specifica dell'errore
                sendReply(context.response(), reply); //invia la risposta al client
            }
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