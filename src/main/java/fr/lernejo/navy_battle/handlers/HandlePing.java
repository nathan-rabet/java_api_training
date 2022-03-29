package fr.lernejo.navy_battle.handlers;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpHandler;

public class HandlePing {
    public HttpHandler pingHandler() {
        return new HttpHandler() {
            @Override
            public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
                String body = "OK";
                exchange.sendResponseHeaders(200, body.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body.getBytes());
                } catch (Exception e) {
                    System.err.println("Error while sending response: " + e.getMessage());
                }
            }
        };
    }
}
