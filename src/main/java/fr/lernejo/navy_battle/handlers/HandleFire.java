package fr.lernejo.navy_battle.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpHandler;

import org.javatuples.Pair;
import org.json.JSONObject;

import fr.lernejo.navy_battle.game.BattleShip;
import fr.lernejo.navy_battle.network.Server;
import fr.lernejo.navy_battle.utils.HttpParser;

public class HandleFire {
    public HttpHandler fireHandler(Server server) {
        return new HttpHandler() {
            public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
                if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    Pair<BattleShip.CONSEQUENCE, Boolean> result;
                    try {
                        result = getGETparams(server, exchange);
                    } catch (Exception e) {
                        sendError(exchange);
                        return;
                    }
                    respondToClient(exchange, result);
                    gameOverEvaluate(server, result);
                }
            }
        };
    }

    private void gameOverEvaluate(Server server, Pair<BattleShip.CONSEQUENCE, Boolean> result) {
        if (server == null)
            return;
        if (!result.getValue1()) {
            server.game.reset(true);
            System.out.println("Game over");
            System.exit(0);
        } else {
            // Hit the client
            server.playWith(server.game.playerUID[0], server.game.playerUID[1]);
        }
    }

    private void respondToClient(com.sun.net.httpserver.HttpExchange exchange,
            Pair<BattleShip.CONSEQUENCE, Boolean> result) throws IOException {
        Map<String, Object> response = new java.util.HashMap<String, Object>();

        response.put("consequence", result.getValue0().toString().toLowerCase());
        response.put("shipLeft", result.getValue1());

        String body = new JSONObject(response).toString();
        // Exchange set header
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        } catch (Exception e) {
            System.err.println("Error while sending response: " + e.getMessage());
        }
    }

    private Pair<BattleShip.CONSEQUENCE, Boolean> getGETparams(Server server,
            com.sun.net.httpserver.HttpExchange exchange) throws Exception {
        if (server == null)
            return null;

        Pair<BattleShip.CONSEQUENCE, Boolean> result;
        Map<String, String> parameters = HttpParser.queryToMap(exchange.getRequestURI().getQuery());
        String cell = parameters.get("cell");
        if (cell == null)
            throw new Exception("Missing parameter: cell");

        System.out.println("cell: " + cell);
        result = server.game.receiveHit(cell);
        return result;
    }

    private void sendError(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        String body = "Error while parsing request";
        exchange.sendResponseHeaders(400, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        } catch (Exception e2) {
            System.err.println("Error while sending response: " + e2.getMessage());
        }
    }
}
