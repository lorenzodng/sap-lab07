package ttt_api_gateway.infrastructure;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import io.vertx.core.json.JsonObject;

//proxy che fornisce i metodi http che i proxy microservizi usano per fare richieste verso i microservizi reali
public class HTTPSyncBaseProxy {

	protected HttpResponse<String> doPost(String uri, JsonObject body) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient(); //crea un client http
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).header("Accept", "application/json").POST(BodyPublishers.ofString(body.toString())).build(); //costruisce una richiesta "post"
		return client.send(request, HttpResponse.BodyHandlers.ofString()); //invia la richiesta al server di destinazione in modo sincrono (il thread resta bloccato fino alla risposta - la risposta è automatica)
	}

	protected HttpResponse<String> doGet(String uri) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient(); //crea un client http
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build(); //costruisce una richiesta "get"
		return client.send(request, HttpResponse.BodyHandlers.ofString()); //invia la richiesta al server di destinazione in modo sincrono (il thread resta bloccato fino alla risposta - la risposta è automatica)invia la richiesta al server di destinazione
	}
}
