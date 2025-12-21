import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServiceChecker {
    private final MonitoredService service;
    private static final DateTimeFormatter LOG_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "monitoring.log";

    public ServiceChecker(MonitoredService service) {
        this.service = service;
    }

    public void check() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(service.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            ServiceStatus newStatus = (responseCode == 200) ? ServiceStatus.UP : ServiceStatus.DOWN;

            updateServiceStatus(newStatus);
            logToFile(service.getId(), newStatus, "HTTP " + responseCode);

        } catch (IOException e) {
            updateServiceStatus(ServiceStatus.DOWN);
            logToFile(service.getId(), ServiceStatus.DOWN, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void updateServiceStatus(ServiceStatus newStatus) {
        service.setStatus(newStatus);
        service.setLastChecked(LocalDateTime.now());
    }

    private void logToFile(String serviceId, ServiceStatus status, String details) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            String logEntry = String.format("[%s] [%s] [%s] %s%n",
                    LocalDateTime.now().format(LOG_FORMATTER),
                    serviceId,
                    status,
                    details);

            bw.write(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}