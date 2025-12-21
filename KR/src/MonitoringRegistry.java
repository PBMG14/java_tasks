import java.util.*;

public class MonitoringRegistry {
    private final Map<String, MonitoredService> services;

    public MonitoringRegistry() {
        this.services = new HashMap<>();
    }

    public void addService(MonitoredService service) {
        services.put(service.getId(), service);
    }

    public MonitoredService getService(String id) {
        return services.get(id);
    }

    public List<MonitoredService> getAllServices() {
        return new ArrayList<>(services.values());
    }

    public boolean removeService(String id) {
        return services.remove(id) != null;
    }

    public void clear() {
        services.clear();
    }

    public int getServiceCount() {
        return services.size();
    }
}
