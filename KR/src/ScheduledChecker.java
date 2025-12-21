public class ScheduledChecker implements Runnable {
    private final ServiceChecker checker;
    private final int intervalSeconds;
    private volatile boolean running;
    private final String serviceId;

    public ScheduledChecker(ServiceChecker checker, int intervalSeconds, String serviceId) {
        this.checker = checker;
        this.intervalSeconds = intervalSeconds;
        this.running = true;
        this.serviceId = serviceId;
    }

    @Override
    public void run() {
        System.out.println("Запущен планировщик для сервиса с ID: " + serviceId);

        while (running) {
            try {
                // Выполняем проверку
                checker.check();

                // Ждем указанный интервал
                Thread.sleep(intervalSeconds * 1000L);

            } catch (InterruptedException e) {
                System.out.println("Планировщик прерван для сервиса: " + serviceId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Ошибка в планировщике для сервиса " + serviceId + ": " + e.getMessage());
            }
        }

        System.out.println("Планировщик остановлен для сервиса: " + serviceId);
    }

    public void stop() {
        running = false;
    }
}