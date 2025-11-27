package ttt_api_gateway.infrastructure;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import common.exagonal.Adapter;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import ttt_api_gateway.application.*;
import ttt_api_gateway.domain.Game;

/*
proxy che collega l'api gateway con il servizio di gioco
flusso di chiamate: client -> server api-gateway -> controller -> proxy gioco -> container/microservizio
 */
@Adapter
public class GameServiceProxy extends HTTPSyncBaseProxy implements GameService  {

	private String serviceAddress; //link del servizio di gioco
    private String wsAddress; //indirizzo (nome) del servizio di gioco
    private int wsPort; //porta del servizio di gioco

    public GameServiceProxy(String serviceAPIEndpoint, String wsAddress, int wsPort) {
		this.serviceAddress = serviceAPIEndpoint;
		this.wsPort = wsPort;
		this.wsAddress = wsAddress;
	}

    //esegue una mossa
    @Override
	public void makeAMove(String gameId, String playerSessionId, int x, int y) throws ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject(); //crea un oggetto json di richiesta
			body.put("x", x); //popola l'oggetto con la coordinata "x" della mossa
			body.put("y", y); //popola l'oggetto con la coordinata "y" della mossa
			HttpResponse<String> response = doPost( serviceAddress + "/api/v1/games/" + gameId + "/" + playerSessionId + "/move", body); //invia una richiesta "post" (creare) al servizio di gioco (vedi il relativo openapi.yaml per la parte dopo "serviceAddress")
			if (response.statusCode() == 200) { //se il codice di risposta è "200"
				return ; //termina
			} else { //altrimenti
				throw new ServiceNotAvailableException(); //lancia un'eccezione
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

    //recupera le informazioni di una partita
	@Override
	public Game getGameInfo(String gameId) throws ServiceNotAvailableException {
		try {
			HttpResponse<String> response = doGet( serviceAddress + "/api/v1/games/" + gameId); //invia una richiesta "get" (recuperare) al servizio di gioco (vedi il relativo openapi.yaml per la parte dopo "serviceAddress")
			if (response.statusCode() == 200) { //se il codice di risposta è "200"
				JsonObject json = new JsonObject(response.body()); //legge il messaggio di risposta
				JsonObject obj = json.getJsonObject("gameInfo"); //estrae le informazioni del campo "gameInfo"
				JsonArray bs = obj.getJsonArray("boardState"); //estrae le informazioni dell'array "boardState"
				List<String> l = new ArrayList<String>(); //crea una lista per i simboli della griglia della partita
				for (var el: bs) { //per ogni elemento dell'array
					l.add(el.toString()); //lo aggiunge alla nuova lista
				}
				return new Game(obj.getString("gameId"), obj.getString("gameState"), l, obj.getString("turn")); //restituisce (al client) la partita creato con l'id, stato e turno corrente
			} else { //altrimenti
				throw new ServiceNotAvailableException(); //lancia un'eccezione
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

    //crea una canale per ricevere gli eventi di gioco
    @Override
    public void createAnEventChannel(String playerSessionId, Vertx vertx) {
        var eb = vertx.eventBus(); //recupera l'event bus (già esistente) dell'istanza vertx (passata come parametro)
        WebSocketClient client = vertx.createWebSocketClient();
        client.connect(wsPort, wsAddress, "/api/v1/events").onSuccess(ws -> { //crea una websocket (canale di comunicazione bidirezionale) con il servizio di gioco, e in caso di successo:
            System.out.println("Connected!");
            ws.textMessageHandler(msg -> { //definisce un handler dei messaggi ricevuti dal servizio di gioco
                eb.publish(playerSessionId, msg); //restituisce sull'event bus del client (relativo alla sessione con id "playerSessionId") il messaggio ricevuto
            });

            JsonObject obj = new JsonObject(); //crea un oggetto json
            obj.put("playerSessionId", playerSessionId); //popola l'oggetto con l'id della sessione giocatore
            ws.writeTextMessage(obj.toString()); //invia al servizio di gioco un messaggio iniziale contenente l'id della sessione giocatore, così da associare la websocket alla sessione del giocatore con quell'id
        }).onFailure(err -> { //altrimenti, in caso di fallimento
            eb.publish(playerSessionId, "error"); //restituisce sull'event bus del client (relativo alla sessione con id "playerSessionId") un messaggio di errore
        });
    }
}
