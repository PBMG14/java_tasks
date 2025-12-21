import java.util.*;
import java.util.concurrent.*;

public class MonitoringScheduler {
    private final MonitoringRegistry registry;
    private final ExecutorService executorService;
    private final Map<String, ScheduledChecker> checkers;
    private final Map<String, Future<?>> futures;

    public MonitoringScheduler(MonitoringRegistry registry) {
        this.registry = registry;
        this.executorService = Executors.newCachedThreadPool();
        this.checkers = new ConcurrentHashMap<>();
        this.futures = new ConcurrentHashMap<>();
    }

    public void startMonitoring() {
        System.out.println("Запуск мониторинга всех сервисов...");

        for (MonitoredService service : registry.getAllServices()) {
            startServiceMonitoring(service);
        }
    }

    public void startServiceMonitoring(MonitoredService service) {
        if (checkers.containsKey(service.getId())) {
            System.out.println("Мониторинг для сервиса " + service.getId() + " уже запущен");
            return;
        }

        ServiceChecker serviceChecker = new ServiceChecker(service);
        ScheduledChecker scheduledChecker = new ScheduledChecker(
                serviceChecker, service.getCheckIntervalSeconds(), service.getId());

        checkers.put(service.getId(), scheduledChecker);
        Future<?> future = executorService.submit(scheduledChecker);
        futures.put(service.getId(), future);

        System.out.println("Мониторинг запущен для сервиса: " + service.getName() + " (ID: " + service.getId() + ")");
    }

    public void stopServiceMonitoring(String serviceId) {
        ScheduledChecker checker = checkers.get(serviceId);
        if (checker != null) {
            checker.stop();
            checkers.remove(serviceId);

            Future<?> future = futures.get(serviceId);
            if (future != null) {
                future.cancel(true);
                futures.remove(serviceId);
            }

            System.out.println("Мониторинг остановлен для сервиса: " + serviceId);
        }
    }

    public void stopAllMonitoring() {
        System.out.println("Остановка всего мониторинга...");

        // Останавливаем все чекеры
        for (ScheduledChecker checker : checkers.values()) {
            checker.stop();
        }

        // Отменяем все Future
        for (Future<?> future : futures.values()) {
            future.cancel(true);
        }

        // Завершаем ExecutorService
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        checkers.clear();
        futures.clear();

        System.out.println("Весь мониторинг остановлен");
    }

    public void addAndStartMonitoring(MonitoredService service) {
        registry.addService(service);
        startServiceMonitoring(service);
    }
}