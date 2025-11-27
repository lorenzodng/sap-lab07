package ttt_lobby_service.infrastructure;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import common.exagonal.Adapter;
import java.net.http.HttpResponse;
import io.vertx.core.json.JsonObject;
import ttt_lobby_service.application.AccountService;
import ttt_lobby_service.application.ServiceNotAvailableException;

/*
implementazione della porta di uscita che collega il sevizio lobby con il servizio degli account
è chiamato "proxy" perchè svolge due ruoli:
1) implementa una porta di uscita
2) fa da intermediario tra due componenti/servizi del sistema
*/
@Adapter
public class AccountServiceProxy implements AccountService {

    private String serviceURI; //url del servizio account

    public AccountServiceProxy(String serviceAPIEndpoint)  {
        this.serviceURI = serviceAPIEndpoint;
    }

    //verifica la validità della password
    @Override
    public boolean isValidPassword(String userName, String password) throws ServiceNotAvailableException {
        try {
            HttpClient client = HttpClient.newHttpClient(); //crea un client http
            JsonObject body = new JsonObject(); //crea un oggetto json di richiesta
            body.put("password", password); //popola l'oggetto con la password
            String isValidReq = serviceURI + "/api/v1/accounts/" + userName + "/check-pwd"; //costruisce il link della richiesta al servizio account
            HttpRequest request = HttpRequest.newBuilder() //costruisce la richiesta specificando:
                    .uri(URI.create(isValidReq)) //link
                    .header("Accept", "application/json") //tipo di risposta attesa
                    .POST(BodyPublishers.ofString(body.toString())) //tipo di richiesta
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); //invia la richiesta al servizio account
            System.out.println("Response Code: " + response.statusCode());
            if (response.statusCode() == 200) { //se il codice di risposta è "200"
                JsonObject json = new JsonObject(response.body()); //legge il messaggio di risposta
                var res = json.getString("result"); //estrae il valore del campo "result"
                if (res.equals("valid-password")) { //se il valore è "valid-password"
                    return true; //restituisce "true"
                } else { //altrimenti
                    return false; //restituisce "false"
                }
            } else { //altrimenti
                System.out.println("POST request failed: " + response.body());
                return false; //restituisce "false"
            }
        } catch (Exception ex) {
            throw new ServiceNotAvailableException();
        }
    }
}