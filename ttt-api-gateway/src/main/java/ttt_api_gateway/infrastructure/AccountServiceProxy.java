package ttt_api_gateway.infrastructure;

import common.exagonal.Adapter;
import java.net.http.HttpResponse;
import io.vertx.core.json.JsonObject;
import ttt_api_gateway.application.AccountService;
import ttt_api_gateway.application.ServiceNotAvailableException;
import ttt_api_gateway.domain.Account;
import ttt_api_gateway.domain.AccountRef;

/*
proxy che collega l'api gateway con il servizio degli account
flusso di chiamate: client -> server api-gateway -> controller -> proxy account -> container/microservizio
 */
@Adapter
public class AccountServiceProxy extends HTTPSyncBaseProxy implements AccountService {

	private String serviceAddress; //link del servizio account

	public AccountServiceProxy(String serviceAPIEndpoint) {
		this.serviceAddress = serviceAPIEndpoint;
	}

    //registra un utente
    @Override
	public AccountRef registerUser(String userName, String password) throws ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject(); //crea un oggetto json di richiesta
			body.put("password", password); //popola l'oggetto con la password
			body.put("userName", userName); //popola l'oggetto con l'username

			HttpResponse<String> response = doPost( serviceAddress + "/api/v1/accounts", body); //invia una richiesta "post" (creare) al servizio account (vedi il relativo openapi.yaml per la parte dopo "serviceAddress")

            if (response.statusCode() == 200) { //se il codice di risposta è "200"
				JsonObject json = new JsonObject(response.body()); //legge il messaggio di risposta
				return new AccountRef(userName, json.getString("accountLink")); //restituisce (al client) l'account creato con username e link (per recuperare le informazioni dell'account)
			} else {  //altrimenti
				throw new ServiceNotAvailableException(); //lancia un'eccezione
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

    //recupera le informazioni di un account
	@Override
	public Account getAccountInfo(String userName) throws ServiceNotAvailableException {
		try {
			HttpResponse<String> response = doGet( serviceAddress + "/api/v1/accounts/" + userName); //invia una richiesta "get" (recuperare) al servizio account (vedi il relativo openapi.yaml per la parte dopo "serviceAddress")
			
			if (response.statusCode() == 200) { //se il codice di risposta è "200"
				JsonObject json = new JsonObject(response.body()); //legge il messaggio di risposta
				JsonObject obj = json.getJsonObject("accountInfo"); //estrae le informazioni del campo "accountInfo"
				return new Account(obj.getString("userName"), obj.getString("password"), obj.getLong("whenCreated")); //restituisce (al client) l'account creato con username, password e data di creazione
			} else { //altrimenti
				throw new ServiceNotAvailableException(); //lancia un'eccezione
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}
}
