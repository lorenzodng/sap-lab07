package ttt_api_gateway.infrastructure;

import java.net.http.HttpResponse;
import common.exagonal.Adapter;
import io.vertx.core.json.JsonObject;
import ttt_api_gateway.application.LobbyService;
import ttt_api_gateway.application.ServiceNotAvailableException;
import ttt_api_gateway.application.TTTSymbol;

/*
proxy che collega l'api gateway con il servizio di lobby
flusso di chiamate: client -> server api-gateway -> controller -> proxy lobby -> container/microservizio
 */
@Adapter
public class LobbyServiceProxy extends HTTPSyncBaseProxy implements LobbyService {

	private String serviceAddress; //link del servizio di lobby
	
	public LobbyServiceProxy(String serviceAPIEndpoint)  {
		this.serviceAddress = serviceAPIEndpoint;		
	}

    //logga l'utente
	@Override
	public String login(String userName, String password) throws ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject(); //crea un oggetto json di richiesta
			body.put("password", password); //popola l'oggetto con la password
			body.put("userName", userName); //popola l'oggetto con l'username

			HttpResponse<String> response = doPost( serviceAddress + "/api/v1/lobby/login", body); //invia una richiesta "post" al servizio lobby (vedi il relativo openapi.yaml per la parte dopo "serviceAddress")

            if (response.statusCode() == 200) { //se il codice di risposta è "200"
				JsonObject json = new JsonObject(response.body()); //legge il messaggio di risposta
				return json.getString("sessionId"); //restituisce (al client) un messaggio con l'id della sessione utente
			} else { //altrimenti
				throw new ServiceNotAvailableException(); //lancia un'eccezione
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

    //crea una nuova partita
	@Override
	public void createNewGame(String sessionId, String gameId) throws ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject(); //crea un oggetto json di richiesta
			body.put("gameId", gameId); //popola l'oggetto con l'id della partita

            HttpResponse<String> response = doPost( serviceAddress + "/api/v1/lobby/user-sessions/" + sessionId + "/create-game", body); //invia una richiesta "post" (creare) al servizio lobby (vedi il relativo openapi.yaml per la parte dopo "serviceAddress")

            if (response.statusCode() == 200) { //se il codice di risposta è "200"
				return; //termina
			} else { //altrimenti
				throw new ServiceNotAvailableException(); //lancia un'eccezione
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

    //fa entrare l'utente in una partita
	@Override
	public String joinGame(String sessionId, String gameId, TTTSymbol symbol) throws ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject(); //crea un oggetto json di richiesta
			body.put("gameId", gameId); //popola l'oggetto con l'id della partita
			body.put("symbol", symbol.toString()); //popola l'oggetto con il simbolo indicato dall'utente

            HttpResponse<String> response = doPost( serviceAddress + "/api/v1/lobby/user-sessions/" + sessionId + "/join-game", body); //invia una richiesta "post" al servizio lobby (vedi il relativo openapi.yaml per la parte dopo "serviceAddress")

            if (response.statusCode() == 200) { //se il codice di risposta è "200"
				JsonObject json = new JsonObject(response.body()); //legge il messaggio di risposta
				return json.getString("playerSessionId"); //restituisce (al client) un messaggio con l'id della sessione giocatore
			} else { //altrimenti
				throw new ServiceNotAvailableException(); //lancia un'eccezione
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}
}
