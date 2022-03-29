package fr.lernejo.navy_battle.network;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.json.JSONObject;

public class Client {
        final String remoteUrlWithPort;
        final HttpClient client;
        public final Server server;

        final String message = "Hello from client";

        public Client(int localPort, String remoteUrlWithPort) throws IOException {
                this.remoteUrlWithPort = remoteUrlWithPort;
                this.server = new Server(localPort);
                this.client = HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_2)
                                .build();

                Map<String, String> response = new HashMap<String, String>();
                response.put("id", RandomString.make(10));
                response.put("url", server.getServerIp());
                response.put("message", message);

                sendResponse(remoteUrlWithPort, response);
        }

        private void sendResponse(String remoteUrlWithPort, Map<String, String> response) {
                String body = new JSONObject(response).toString();
                HttpRequest requetePost = HttpRequest.newBuilder()
                                .uri(URI.create(remoteUrlWithPort + "/api/game/start"))
                                .setHeader("Accept", "application/json")
                                .setHeader("Content-Type", "application/json")
                                .POST(BodyPublishers
                                                .ofString(body))
                                .build();

                client.sendAsync(requetePost, HttpResponse.BodyHandlers.ofString())
                                .thenApply(HttpResponse::body)
                                .thenAccept(System.out::println);
        }

}
