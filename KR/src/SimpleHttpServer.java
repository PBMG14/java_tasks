import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;

public class SimpleHttpServer {
    private final int port;
    private final MonitoringScheduler scheduler;
    private final MonitoringRegistry registry;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running;

    public SimpleHttpServer(int port, MonitoringScheduler scheduler, MonitoringRegistry registry) {
        this.port = port;
        this.scheduler = scheduler;
        this.registry = registry;
        this.threadPool = Executors.newFixedThreadPool(10);
        this.running = false;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("HTTP сервер запущен на порту " + port);

        // Запускаем основной цикл в отдельном потоке
        new Thread(this::acceptConnections).start();
    }

    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Ошибка при принятии соединения: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Читаем HTTP-заголовок
            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println("Получен запрос: " + requestLine);

            // Парсим метод и путь
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendError(out, 400, "Bad Request");
                return;
            }

            String method = requestParts[0];
            String path = requestParts[1];

            // Читаем тело запроса для POST
            StringBuilder requestBody = new StringBuilder();
            String line;
            boolean readingBody = false;
            int contentLength = 0;

            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    // После пустой строки начинается тело
                    readingBody = true;
                    if (contentLength > 0) {
                        char[] body = new char[contentLength];
                        in.read(body, 0, contentLength);
                        requestBody.append(body);
                    }
                    break;
                }
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                }
            }

            // Обрабатываем запрос
            handleRequest(method, path, requestBody.toString(), out);

        } catch (IOException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Игнорируем ошибку закрытия
            }
        }
    }

    private void handleRequest(String method, String path, String body, PrintWriter out) {
        try {
            if (path.equals("/api/services") && method.equals("GET")) {
                handleGetAllServices(out);
            } else if (path.matches("/api/services/[a-zA-Z0-9]+") && method.equals("GET")) {
                String id = path.substring("/api/services/".length());
                handleGetService(id, out);
            } else if (path.equals("/api/services") && method.equals("POST")) {
                handleAddService(body, out);
            } else if (path.equals("/api/status") && method.equals("GET")) {
                handleGetStatus(out);
            } else {
                sendError(out, 404, "Not Found");
            }
        } catch (Exception e) {
            sendError(out, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetAllServices(PrintWriter out) {
        StringBuilder json = new StringBuilder("[");
        List<MonitoredService> services = registry.getAllServices();

        for (int i = 0; i < services.size(); i++) {
            MonitoredService service = services.get(i);
            json.append(String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"status\":\"%s\",\"url\":\"%s\",\"lastChecked\":\"%s\"}",
                    service.getId(),
                    service.getName(),
                    service.getStatus(),
                    service.getUrl(),
                    service.getLastChecked()
            ));
            if (i < services.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        sendJsonResponse(out, json.toString());
    }

    private void handleGetService(String id, PrintWriter out) {
        MonitoredService service = registry.getService(id);
        if (service == null) {
            sendError(out, 404, "Service not found");
            return;
        }

        String json = String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"status\":\"%s\",\"url\":\"%s\"," +
                        "\"checkInterval\":%d,\"lastChecked\":\"%s\"}",
                service.getId(),
                service.getName(),
                service.getStatus(),
                service.getUrl(),
                service.getCheckIntervalSeconds(),
                service.getLastChecked()
        );

        sendJsonResponse(out, json);
    }

    private void handleAddService(String body, PrintWriter out) {
        try {
            // Простой парсинг JSON (для учебных целей)
            // Ожидаем формат: {"id":"5","name":"New","url":"https://...","interval":10}
            String id = extractValue(body, "id");
            String name = extractValue(body, "name");
            String url = extractValue(body, "url");
            String intervalStr = extractValue(body, "interval");

            if (id == null || name == null || url == null || intervalStr == null) {
                sendError(out, 400, "Missing required fields");
                return;
            }

            int interval = Integer.parseInt(intervalStr);
            MonitoredService service = new MonitoredService(id, name, url, interval);

            // Добавляем и запускаем мониторинг
            scheduler.addAndStartMonitoring(service);

            sendJsonResponse(out, "{\"status\":\"added\",\"id\":\"" + id + "\"}");

        } catch (Exception e) {
            sendError(out, 400, "Invalid request: " + e.getMessage());
        }
    }

    private void handleGetStatus(PrintWriter out) {
        List<MonitoredService> services = registry.getAllServices();
        int up = 0, down = 0, unknown = 0;

        for (MonitoredService service : services) {
            switch (service.getStatus()) {
                case UP: up++; break;
                case DOWN: down++; break;
                case UNKNOWN: unknown++; break;
            }
        }

        String json = String.format(
                "{\"total\":%d,\"up\":%d,\"down\":%d,\"unknown\":%d}",
                services.size(), up, down, unknown
        );

        sendJsonResponse(out, json);
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);

        if (m.find()) {
            return m.group(1);
        }

        // Пробуем найти числовое значение
        pattern = "\"" + key + "\":([0-9]+)";
        p = java.util.regex.Pattern.compile(pattern);
        m = p.matcher(json);

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    private void sendJsonResponse(PrintWriter out, String json) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + json.length());
        out.println("Connection: close");
        out.println();
        out.println(json);
    }

    private void sendError(PrintWriter out, int code, String message) {
        String response = "{\"error\":\"" + message + "\"}";
        out.println("HTTP/1.1 " + code + " " + getStatusMessage(code));
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + response.length());
        out.println("Connection: close");
        out.println();
        out.println(response);
    }

    private String getStatusMessage(int code) {
        switch (code) {
            case 200: return "OK";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Игнорируем
        }

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("HTTP сервер остановлен");
    }
}