package ttt_api_gateway.infrastructure;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

import io.vertx.core.json.JsonObject;

public class HTTPSyncBaseProxy {

	protected HttpResponse<String> doPost(String uri, JsonObject body) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri))
					.header("Accept", "application/json").POST(BodyPublishers.ofString(body.toString())).build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	protected HttpResponse<String> doGet(String uri) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}
	
}
