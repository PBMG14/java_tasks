import java.time.LocalDateTime;

public class MonitoredService {
    private final String id;
    private String name;
    private String url;
    private int checkIntervalSeconds;
    private ServiceStatus status;
    private LocalDateTime lastChecked;

    public MonitoredService(String id, String name, String url, int checkIntervalSeconds) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.checkIntervalSeconds = checkIntervalSeconds;
        this.status = ServiceStatus.UNKNOWN;
        this.lastChecked = null;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getCheckIntervalSeconds() { return checkIntervalSeconds; }
    public void setCheckIntervalSeconds(int checkIntervalSeconds) {
        this.checkIntervalSeconds = checkIntervalSeconds;
    }
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
    public LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }

    @Override
    public String toString() {
        return String.format("Service{id='%s', name='%s', status=%s, lastChecked=%s}",
                id, name, status, lastChecked);
    }
}
