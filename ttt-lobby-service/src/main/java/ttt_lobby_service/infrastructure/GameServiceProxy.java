package ttt_lobby_service.infrastructure;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import common.exagonal.Adapter;
import java.net.http.HttpResponse;
import io.vertx.core.json.JsonObject;
import ttt_lobby_service.application.CreateGameFailedException;
import ttt_lobby_service.application.GameAlreadyPresentException;
import ttt_lobby_service.application.GameService;
import ttt_lobby_service.application.InvalidJoinGameException;
import ttt_lobby_service.application.JoinGameFailedException;
import ttt_lobby_service.application.ServiceNotAvailableException;
import ttt_lobby_service.domain.TTTSymbol;
import ttt_lobby_service.domain.UserId;

/*
implementazione della porta di uscita che collega il sevizio lobby con il servizio di gioco
è chiamato "proxy" perchè svolge due ruoli:
1) implementa una porta di uscita
2) fa da intermediario tra due componenti/servizi del sistema
*/
@Adapter
public class GameServiceProxy implements GameService {

    private String serviceURI; //url del servizio di gioco

    public GameServiceProxy(String serviceAPIEndpoint)  {
        this.serviceURI = serviceAPIEndpoint;
    }

    //cera una nuova partita
    @Override
    public void createNewGame(String gameId) throws GameAlreadyPresentException, CreateGameFailedException, ServiceNotAvailableException {
        try {
            HttpClient client = HttpClient.newHttpClient(); //crea un client http
            JsonObject body = new JsonObject(); //crea un oggetto json di richiesta
            body.put("gameId", gameId); //popola l'oggetto con l'id della partita
            String gameResEndpoint = serviceURI + "/api/v1/games"; //costruisce il link della richiesta al servizio di gioco
            HttpRequest request = HttpRequest.newBuilder() //costruisce la richiesta specificando:
                    .uri(URI.create(gameResEndpoint)) //link
                    .header("Accept", "application/json") //tipo di risposta attesa
                    .POST(BodyPublishers.ofString(body.toString())) //tipo di richiesta
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); //invia la richiesta al servizio di gioco
            System.out.println("Response Code: " + response.statusCode());
            if (response.statusCode() == 200) { //se il codice di risposta è "200"
                JsonObject json = new JsonObject(response.body()); //legge il messaggio di risposta
                var res = json.getString("result"); //estrae il valore del campo "result"
                if (res.equals("error")) { //se il valore è "error"
                    var err = json.getString("error"); //estrae il valore del campo "error"
                    if (err.equals("game-already-present")) { //se il valore è "game-already-present"
                        throw new GameAlreadyPresentException(); //lancia un'eccezione
                    } else { //altrimenti
                        throw new CreateGameFailedException(); //lancia un'eccezione (diversa)
                    }
                }
            } else { //altrimenti
                System.out.println("POST request failed: " + response.body());
                throw new ServiceNotAvailableException(); //lancia un'eccezione
            }
        } catch (Exception ex) {
            throw new ServiceNotAvailableException();
        }
    }

    //fa entrare un utente in una partita
    @Override
    public String joinGame(UserId userId, String gameId, TTTSymbol symbol) throws InvalidJoinGameException, JoinGameFailedException, ServiceNotAvailableException {
        try {
            HttpClient client = HttpClient.newHttpClient(); //crea un client http
            JsonObject body = new JsonObject(); //crea un oggetto json di richiesta
            body.put("userId", userId.id()); //popola l'oggetto con l'id dell'utente
            body.put("symbol", symbol.equals(TTTSymbol.X) ? "X" : "O"); //popola l'oggetto con il simbolo del giocatore
            String joinGameEndpoint = serviceURI + "/api/v1/games/" + gameId + "/join"; //costruisce il link della richiesta al servizio di gioco
            HttpRequest request = HttpRequest.newBuilder() //costruisce la richiesta specificando:
                    .uri(URI.create(joinGameEndpoint)) //link
                    .header("Accept", "application/json") //tipo di risposta attesa
                    .POST(BodyPublishers.ofString(body.toString())) //tipo di richiesta
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); //invia la richiesta al servizio account
            System.out.println("Response Code: " + response.statusCode());
            if (response.statusCode() == 200) { //se il codice di risposta è "200"
                JsonObject json = new JsonObject(response.body()); //legge il messaggio di risposta
                var res = json.getString("result"); //estrae il valore del campo "result"
                if (res.equals("ok")) { //se il valore è "ok"
                    var playerSessionId = json.getString("playerSessionId"); //recupera l'id della sessione del giocatore
                    return playerSessionId; //restituisce l'id della sessione del giocatore
                } else if (res.equals("error")) { //altrimenti se il valore è "error"
                    throw new InvalidJoinGameException(); //lancia un'eccezione
                } else { //altrimenti
                    throw new JoinGameFailedException(); //lancia un'eccezione (diversa)
                }
            } else { //altrimenti
                System.out.println("POST request failed: " + response.body());
                throw new ServiceNotAvailableException(); //lancia un'eccezione
            }
        } catch (Exception ex) {
            throw new ServiceNotAvailableException();
        }
    }
}