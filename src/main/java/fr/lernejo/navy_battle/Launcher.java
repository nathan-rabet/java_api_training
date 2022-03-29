package fr.lernejo.navy_battle;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.json.JSONObject;

import fr.lernejo.navy_battle.network.Server;

public class Launcher {
    public static void main(String[] args) throws InterruptedException, IOException {
        Server server;
        if (args.length == 1 || args.length == 2) {
            int port = Integer.parseInt(args[0]);
            server = new Server(port);
            if (args.length == 2)
                clientSide(args, server);
            server.latch.await();
        } else {
            System.err.println("Usage: <port> or <port> <serverUrl>");
            System.exit(1);
        }
    }

    private static void clientSide(String[] args, Server server) {
        String remoteHost = args[1];
        String userId = RandomString.make(10);

        server.setUserUID(userId, remoteHost);

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        sendClientHello(server, remoteHost, userId, client);
    }

    private static void sendClientHello(Server server, String remoteHost, String userId, HttpClient client) {
        Map<String, String> request = new HashMap<String, String>();
        request.put("id", userId);
        try {
            request.put("url", server.getServerIp());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        HttpRequest requetePost = sendRequestHello(remoteHost, request);

        client.sendAsync(requetePost, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpRequest sendRequestHello(String remoteHost, Map<String, String> request) {
        request.put("message", "Hello from client");
        String body = new JSONObject(request).toString();
        HttpRequest requetePost = HttpRequest.newBuilder()
                .uri(URI.create(remoteHost + "/api/game/start"))
                .setHeader("Accept", "application/json")
                .setHeader("Content-Type", "application/json")
                .POST(BodyPublishers
                        .ofString(body))
                .build();
        return requetePost;
    }
}
