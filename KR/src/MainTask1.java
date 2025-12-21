import java.io.FileWriter;
import java.io.IOException;

public class MainTask1 {
    public static void main(String[] args) {
        // Очищаем лог-файл перед началом
        clearLogFile();

        // Создаем реестр
        MonitoringRegistry registry = new MonitoringRegistry();

        // Добавляем тестовые сервисы
        registry.addService(new MonitoredService("1", "GitHub API",
                "https://api.github.com", 30));
        registry.addService(new MonitoredService("2", "JSONPlaceholder",
                "https://jsonplaceholder.typicode.com/posts/1", 30));
        registry.addService(new MonitoredService("3", "Недоступный сервис",
                "http://nonexistent.example.com", 30));

        // Проверяем каждый сервис
        System.out.println("=== Проверка сервисов ===");
        for (MonitoredService service : registry.getAllServices()) {
            ServiceChecker checker = new ServiceChecker(service);
            System.out.println("Проверка: " + service.getName());
            checker.check();

            // Выводим результат
            System.out.println("  Статус: " + service.getStatus());
            System.out.println("  Время проверки: " + service.getLastChecked());
            System.out.println();
        }

        // Выводим итоговый статус всех сервисов
        System.out.println("=== Итоговый статус ===");
        for (MonitoredService service : registry.getAllServices()) {
            System.out.println(service);
        }
    }

    private static void clearLogFile() {
        try {
            new FileWriter("monitoring.log", false).close();
        } catch (IOException e) {
            // Игнорируем, если файла нет
        }
    }
}