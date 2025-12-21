public class MainTask2 {
    public static void main(String[] args) {
        System.out.println("=== Задача 2: Многопоточный планировщик ===");

        // Создаем реестр
        MonitoringRegistry registry = new MonitoringRegistry();

        // Добавляем сервисы с разными интервалами
        registry.addService(new MonitoredService("1", "GitHub API",
                "https://api.github.com", 5));  // Проверка каждые 5 секунд

        registry.addService(new MonitoredService("2", "JSONPlaceholder",
                "https://jsonplaceholder.typicode.com/posts/1", 10));  // 10 секунд

        registry.addService(new MonitoredService("3", "Test Service",
                "http://httpbin.org/delay/1", 15));  // 15 секунд

        // Создаем и запускаем планировщик
        MonitoringScheduler scheduler = new MonitoringScheduler(registry);
        scheduler.startMonitoring();

        // Ждем 30 секунд
        System.out.println("Мониторинг работает 30 секунд...");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Добавляем новый сервис динамически
        System.out.println("\nДинамически добавляем новый сервис...");
        MonitoredService newService = new MonitoredService("4", "New API",
                "https://api.agify.io?name=alex", 7);
        scheduler.addAndStartMonitoring(newService);

        // Ждем еще 20 секунд
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Останавливаем мониторинг для одного сервиса
        System.out.println("\nОстанавливаем мониторинг для сервиса 2...");
        scheduler.stopServiceMonitoring("2");

        // Ждем 10 секунд
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Грациозно останавливаем всё
        System.out.println("\nОстанавливаем весь мониторинг...");
        scheduler.stopAllMonitoring();

        System.out.println("\n=== Программа завершена ===");
        System.out.println("Проверьте файл monitoring.log для просмотра логов");
    }
}