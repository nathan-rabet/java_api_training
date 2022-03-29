package fr.lernejo.navy_battle.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpHandler;

import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.json.JSONObject;

import fr.lernejo.navy_battle.network.Server;

public class HandleStart {
    public HttpHandler startHandler(Server server) {
        if (server == null)
            return null;
        return new HttpHandler() {
            public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    try {
                        getJsonFromExchange(server, exchange);
                    } catch (Exception e) {
                        sendError(exchange);
                    }
                    respondToCLient(server, exchange);
                } else
                    sendErrorMsg(exchange);
            }
        };
    }

    private void sendError(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        String body = "Error while parsing JSON";
        exchange.sendResponseHeaders(400, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        } catch (Exception e2) {
            System.err.println("Error while sending response: " + e2.getMessage());
        }
        return;
    }

    private void getJsonFromExchange(Server server, com.sun.net.httpserver.HttpExchange exchange)
            throws IOException {
        JSONObject json;
        json = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
        System.out.println("message: " + json.getString("message"));

        server.game.reset(true);
        server.setUserUID(json.getString("id"), json.getString("url"));
        server.playWith(server.game.playerUID[0], server.game.playerUID[1]);
    }

    private void sendErrorMsg(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        String body = "Only POST requests are allowed";
        exchange.sendResponseHeaders(405, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        } catch (Exception e) {
            System.err.println("Error while sending response: " + e.getMessage());
        }
    }

    private void respondToCLient(Server server, com.sun.net.httpserver.HttpExchange exchange)
            throws IOException {
        Map<String, String> response = new java.util.HashMap<String, String>();
        RandomString randomString = new RandomString(10);
        response.put("id", randomString.nextString());
        response.put("url", "http://" + exchange.getLocalAddress().getHostName() + ":" + server.bindPort);
        response.put("message", "Hello from server");
        String body = new JSONObject(response).toString();
        exchange.sendResponseHeaders(202, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        } catch (Exception e) {
            System.err.println("Error while sending response: " + e.getMessage());
        }
    }
}
