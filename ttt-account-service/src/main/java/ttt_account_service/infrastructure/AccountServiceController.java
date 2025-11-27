package ttt_account_service.infrastructure;

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
import ttt_account_service.application.AccountAlreadyPresentException;
import ttt_account_service.application.AccountNotFoundException;
import ttt_account_service.application.AccountService;

/*
controller di backend (intermediario client <-> servizio account):
client -> controller che utilizza un server -> servizio account
 */
public class AccountServiceController extends VerticleBase {

    private int port; //porta su cui il server ascolta le richieste http
    static Logger logger = Logger.getLogger("[AccountServiceController]");
    static final String API_VERSION = "v1";
    static final String ACCOUNTS_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts"; //rotta per creare un nuovo account
    static final String ACCOUNT_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId"; //rotta per recuperare le informazioni di un account
    static final String CHECK_PWD_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId/check-pwd"; //rotta per verificare la validità della password di un account
    private AccountService accountService; //servizio account

    public AccountServiceController(AccountService service, int port) {
        this.port = port;
        logger.setLevel(Level.INFO);
        this.accountService = service;
    }

    public Future<?> start() {
        logger.log(Level.INFO, "TTT Game Service initializing...");
        HttpServer server = vertx.createHttpServer(); //crea un sever http

        Router router = Router.router(vertx); //router per l'instradamento delle richieste http
        router.route(HttpMethod.POST, ACCOUNTS_RESOURCE_PATH).handler(this::createAccount); //associa alla rotta per creare un nuovo account il relativo metodo
        router.route(HttpMethod.GET, ACCOUNT_RESOURCE_PATH).handler(this::getAccountInfo); //associa alla rotta per recuperare le informazioni di un account il relativo metodo
        router.route(HttpMethod.POST, CHECK_PWD_RESOURCE_PATH).handler(this::checkAccountPassword); //associa alla rotta per verificare la validità della password di un account il relativo metodo

        router.route("/public/*").handler(StaticHandler.create()); //gestisce le richieste del client che iniziano con "public", relative all'aspetto della pagina web

        var fut = server.requestHandler(router).listen(port); //avvia il server sulla porta specificata
        fut.onSuccess(res -> { //in caso di avvio con successo
            logger.log(Level.INFO, "TTT Account Service ready - port: " + port); //stampa un messaggio di log
        });

        return fut; //restituisce la future
    }

    //crea un account utente
    protected void createAccount(RoutingContext context) { //context è l'oggetto che rappresenta la richiesta http
        logger.log(Level.INFO, "create a new account");
        context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
            logger.log(Level.INFO, "Payload: " + userInfo);
            var userName = userInfo.getString("userName"); //estrae il valore del campo "userName"
            var password = userInfo.getString("password"); //estrae il valore del campo "password"
            var reply = new JsonObject(); //crea un oggetto json di risposta al client
            try {
                accountService.registerUser(userName, password); //crea un nuovo account utente
                reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
                var accountPath = ACCOUNT_RESOURCE_PATH.replace(":accountId", userName); //costruisce il link per le informazioni dell'account
                reply.put("accountLink", accountPath); //popola l'oggetto con il link alle informazioni dell'account
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (AccountAlreadyPresentException ex) {
                reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
                reply.put("error", ex.getMessage()); //popola l'oggetto con la specifica dell'errore
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (Exception ex1) {
                sendError(context.response()); //invia un errore al client
            }
        });
    }

    //recupera le informazioni di un account
    protected void getAccountInfo(RoutingContext context) {
        logger.log(Level.INFO, "get account info");
        var userName = context.pathParam("accountId"); //estrae il valore del campo "userName"
        var reply = new JsonObject(); //crea un oggetto json di risposta al client
        try {
            var acc = accountService.getAccountInfo(userName); //recupera l'account associato a "userName"
            reply.put("result", "ok"); //popola l'oggetto con un'informazione di successo
            var accJson = new JsonObject(); //crea un oggetto json account per memorizzare le informazioni dell'account
            accJson.put("userName", acc.getUserName()); //popola l'oggetto account con il relativo username
            accJson.put("password", acc.getPassword()); //popola l'oggetto account con la relativa password
            accJson.put("whenCreated", acc.getWhenCreated()); //popola l'oggetto account con la relativa data di creazione
            reply.put("accountInfo", accJson); //popola l'oggetto di risposta con l'oggetto account
            sendReply(context.response(), reply); //invia la risposta al client
        } catch (AccountNotFoundException ex) {
            reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
            reply.put("error", "account-not-present"); //popola l'oggetto con la specifica dell'errore
            sendReply(context.response(), reply); //invia la risposta al client
        } catch (Exception ex1) {
            sendError(context.response()); //invia un errore al client
        }
    }

    //verifica la validità della password dell'account
    protected void checkAccountPassword(RoutingContext context) {
        logger.log(Level.INFO, "check account password");
        context.request().handler(buf -> { //prende il body del messaggio http inviato dal client
            var userName = context.pathParam("accountId"); //estrae (dall'url) il valore del campo "accountId"
            JsonObject userInfo = buf.toJsonObject(); //converte il body in un oggetto json
            var password = userInfo.getString("password"); //estrae il valore del campo "password"
            var reply = new JsonObject(); //crea un oggetto json di risposta al client
            try {
                var res = accountService.isValidPassword(userName, password); //verifica la validità della password
                if (res) { //se è corretta
                    reply.put("result", "valid-password"); //popola l'oggetto con un'informazione di correttezza
                } else {
                    reply.put("result", "invalid-password"); //popola l'oggetto con un'informazione di incorrettezza
                }
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (AccountNotFoundException ex) {
                reply.put("result", "error"); //popola l'oggetto con un'informazione di errore
                reply.put("error", "account-not-present"); //popola l'oggetto con la specifica dell'errore
                sendReply(context.response(), reply); //invia la risposta al client
            } catch (Exception ex1) {
                sendError(context.response()); //invia un errore al client
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
