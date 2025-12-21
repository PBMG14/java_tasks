import java.io.IOException;

public class MainTask3 {
    public static void main(String[] args) {
        System.out.println("=== Задача 3: HTTP-сервер управления ===");

        try {
            // Создаем компоненты системы
            MonitoringRegistry registry = new MonitoringRegistry();
            MonitoringScheduler scheduler = new MonitoringScheduler(registry);
            SimpleHttpServer server = new SimpleHttpServer(8080, scheduler, registry);

            // Добавляем начальные сервисы
            registry.addService(new MonitoredService("1", "GitHub API",
                    "https://api.github.com", 30));
            registry.addService(new MonitoredService("2", "JSONPlaceholder",
                    "https://jsonplaceholder.typicode.com/posts/1", 30));

            // Запускаем мониторинг
            scheduler.startMonitoring();

            // Запускаем HTTP-сервер
            server.start();

            System.out.println("Система запущена!");
            System.out.println("Доступные команды:");
            System.out.println("  GET  http://localhost:8080/api/services");
            System.out.println("  GET  http://localhost:8080/api/services/{id}");
            System.out.println("  POST http://localhost:8080/api/services");
            System.out.println("  GET  http://localhost:8080/api/status");
            System.out.println("\nНажмите Enter для остановки...");

            // Ждем Enter для остановки
            System.in.read();

            // Останавливаем всё
            System.out.println("\nОстановка системы...");
            scheduler.stopAllMonitoring();
            server.stop();

        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== Программа завершена ===");
    }
}