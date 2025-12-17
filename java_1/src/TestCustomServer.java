import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TestCustomServer {
    private static final Set<String> validTokens = new HashSet<>();
    private static final Random random = new Random();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/hello", new HelloHandler());
        server.createContext("/ping", new PingHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/image", new ImageHandler());
        server.createContext("/game", new GameHandler());
        server.createContext("/game_rock", new GameRockHandler());
        server.createContext("/game_paper", new GamePaperHandler());
        server.createContext("/game_scissors", new GameScissorsHandler());
        server.createContext("/delete", new DeleteHandler());
        server.start();
        System.out.println("Server started on http://localhost:8000/");
    }

    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello world!";
            sendHtmlResponse(exchange, response);
        }
    }

    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Pong!";
            sendHtmlResponse(exchange, response);
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = generateToken();
            validTokens.add(token);
            String html = "<html><body>" +
                    "<h1>Your Token: " + token + "</h1>" +
                    "<ul>" +
                    "<li><a href='/image?token=" + token + "'>Access Image</a></li>" +
                    "<li><a href='/game?token=" + token + "'>Play Game</a></li>" +
                    "<li><a href='/delete?token=" + token + "'>Delete File</a></li>" +
                    "</ul>" +
                    "</body></html>";

            sendHtmlResponse(exchange, html);
        }

        private String generateToken() {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder token = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                token.append(chars.charAt(random.nextInt(chars.length())));
            }
            return token.toString();
        }
    }

    static class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = getTokenFromRequest(exchange);

            if (token == null || !validTokens.contains(token)) {
                sendErrorHtml(exchange, "Access Denied. Get token from /login");
                return;
            }

            File file = new File("image.png");
            if (!file.exists()) {
                sendErrorHtml(exchange, "File image.png not found");
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class GameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = getTokenFromRequest(exchange);

            if (token == null || !validTokens.contains(token)) {
                sendErrorHtml(exchange, "Access Denied. Get token from /login");
                return;
            }

            String html = "<html><body>" +
                    "<h1>Rock Paper Scissors Game</h1>" +
                    "<p>Your token: " + token + "</p>" +
                    "<p>Make your choice:</p>" +
                    "<ul>" +
                    "<li><a href='/game_rock?token=" + token + "'>Rock</a></li>" +
                    "<li><a href='/game_paper?token=" + token + "'>Paper</a></li>" +
                    "<li><a href='/game_scissors?token=" + token + "'>Scissors</a></li>" +
                    "</ul>" +
                    "</body></html>";

            sendHtmlResponse(exchange, html);
        }
    }

    static class GameRockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleGameChoice(exchange, "rock");
        }
    }

    static class GamePaperHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleGameChoice(exchange, "paper");
        }
    }

    static class GameScissorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleGameChoice(exchange, "scissors");
        }
    }

    private static void handleGameChoice(HttpExchange exchange, String playerChoice) throws IOException {
        String token = getTokenFromRequest(exchange);

        if (token == null || !validTokens.contains(token)) {
            sendErrorHtml(exchange, "Access Denied");
            return;
        }

        String[] choices = {"rock", "paper", "scissors"};
        String computerChoice = choices[random.nextInt(3)];

        String result = determineWinner(playerChoice, computerChoice);

        String html = "<html><body>" +
                "<h1>Game Result</h1>" +
                "<p>Your choice: <strong>" + playerChoice.toUpperCase() + "</strong></p>" +
                "<p>Computer choice: <strong>" + computerChoice.toUpperCase() + "</strong></p>" +
                "<h2>" + result + "</h2>" +
                "<p><a href='/game?token=" + token + "'>Play Again</a></p>" +
                "</body></html>";

        sendHtmlResponse(exchange, html);
    }

    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = getTokenFromRequest(exchange);
            String method = exchange.getRequestMethod();

            if (token == null || !validTokens.contains(token)) {
                sendErrorHtml(exchange, "Access Denied. Get token from /login");
                return;
            }

            if (!method.equals("DELETE")) {
                String html = "<html><body>" +
                        "<h1>Delete File</h1>" +
                        "<p>Your token: " + token + " </p>" +
                        "<pre>curl.exe -X DELETE -H \"X-Auth-Token: " + token + "\" http://localhost:8000/delete</pre>" +
                        "</body></html>";
                sendHtmlResponse(exchange, html);
                return;
            }

            File file = new File("failfordelete.txt");
            String response;

            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    response = "<html><body><h1>File deleted successfully</h1>" +
                            "<p>File: failfordelete.txt</p>" +
                            "<p><a href='/login'>Get new token</a></p></body></html>";
                } else {
                    response = "<html><body><h1>Error deleting file</h1></body></html>";
                }
            } else {
                response = "<html><body><h1>File not found</h1>" +
                        "<p>File failfordelete.txt doesn't exist</p>" +
                        "<p>Current directory: " + System.getProperty("user.dir") + "</p>" +
                        "<p>Looking for file at: " + file.getAbsolutePath() + "</p></body></html>";
            }

            sendHtmlResponse(exchange, response);
        }
    }

    private static String getTokenFromRequest(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("token=")) {
            return query.substring(6);
        }

        String headerToken = exchange.getRequestHeaders().getFirst("X-Auth-Token");
        if (headerToken != null) {
            return headerToken;
        }

        return null;
    }

    private static String determineWinner(String player, String computer) {
        if (player.equals(computer)) {
            return "Draw!";
        }

        switch (player) {
            case "rock":
                return computer.equals("scissors") ? "You Win!" : "You Lose!";
            case "paper":
                return computer.equals("rock") ? "You Win!" : "You Lose!";
            case "scissors":
                return computer.equals("paper") ? "You Win!" : "You Lose!";
            default:
                return "Error";
        }
    }

    private static void sendHtmlResponse(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, html.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(html.getBytes());
        os.close();
    }

    private static void sendErrorHtml(HttpExchange exchange, String message) throws IOException {
        String html = "<html><body><h1>Error</h1><p>" + message + "</p></body></html>";
        sendHtmlResponse(exchange, html);
    }
}