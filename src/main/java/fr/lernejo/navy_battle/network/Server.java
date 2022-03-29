package fr.lernejo.navy_battle.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import fr.lernejo.navy_battle.game.BattleShip;
import fr.lernejo.navy_battle.game.PrintBoard;
import fr.lernejo.navy_battle.handlers.HandleFire;
import fr.lernejo.navy_battle.handlers.HandlePing;
import fr.lernejo.navy_battle.handlers.HandleStart;

public class Server {
    public final int bindPort;
    private final HttpServer server;
    public final BattleShip game;
    public final CountDownLatch latch;

    public Server(int bindPort) throws IOException {
        this.latch = new CountDownLatch(1);
        this.bindPort = bindPort;
        this.game = new BattleShip();
        this.server = HttpServer.create(new java.net.InetSocketAddress(bindPort), 0);

        ExecutorService executor = new java.util.concurrent.ThreadPoolExecutor(
                1,
                1,
                60L,
                java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.LinkedBlockingQueue<Runnable>());

        initAPI(executor);

    }

    private void initAPI(ExecutorService executor) {
        server.setExecutor(executor);
        server.createContext("/ping", (new HandlePing()).pingHandler());
        server.createContext("/api/game/start", (new HandleStart()).startHandler(this));
        server.createContext("/api/game/fire", (new HandleFire()).fireHandler(this));
        server.start();
    }

    public void setUserUID(String userId, String remoteHost) {
        game.playerUID[0] = userId;
        game.playerUID[1] = remoteHost;
    }

    public String getServerIp() throws UnknownHostException {
        return "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + bindPort;
    }

    public void playWith(String id, String url) {
        // Select 2 random integer not already in hitmap
        Random random = new Random();
        int x;
        int y;
        // x & y must not be true in hitmap
        do {
            x = random.nextInt(game.height);
            y = random.nextInt(game.width);
        } while (game.hitmap[x][y]);

        game.hitmap[x][y] = true;
        (new PrintBoard()).printBoard(game);

        sendPlayWithResponse(url, x, y);
    }

    private void sendPlayWithResponse(String url, int x, int y) {
        HttpResponse<String> response;
        try {
            response = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .build().send(HttpRequest.newBuilder()
                            .uri(URI.create(url + "/api/game/fire?cell=" + game.coordinatesToString(x, y)))
                            .setHeader("Accept", "application/json")
                            .setHeader("Content-Type", "application/json")
                            .build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e1) {
            System.err.println("PLAYWITH : Error while sending request: " + e1.getMessage());
            return;
        }
        gameOverEvaluate(response);
    }

    private void gameOverEvaluate(HttpResponse<String> response) {
        try {
            JSONObject json = new JSONObject(response.body().toString());
            boolean shipLeft = json.getBoolean("shipLeft");
            if (!shipLeft) {
                game.reset(true);
                System.out.println("I WON !!!!!!");
                System.exit(0);
            }
        } catch (Exception e) {
            System.err.println("PLAYWITH : Error while parsing response: " + e.getMessage());
        }
    }
}
