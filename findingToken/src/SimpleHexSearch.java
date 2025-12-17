import java.util.*;
import java.util.concurrent.*;

public class SimpleHexSearch {
    private static final String TARGET;
    private static final int LENGTH = 7;

    static {
        Random random = new Random();
        char[] hex = "0123456789abcdef".toCharArray();
        StringBuilder target = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            target.append(hex[random.nextInt(16)]);
        }
        TARGET = target.toString();
        System.out.println("Ищем: " + TARGET);
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        long totalCombinations = (long) Math.pow(16, LENGTH);
        long chunkSize = totalCombinations / threads;

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final long start = i * chunkSize;
            final long end = (i == threads - 1) ? totalCombinations : start + chunkSize;
            final int threadNum = i;

            futures.add(executor.submit(() -> {
                Random random = new Random(threadNum);

                for (long j = start; j < end; j++) {
                    StringBuilder seq = new StringBuilder();
                    for (int k = 0; k < LENGTH; k++) {
                        seq.append("0123456789abcdef".charAt(random.nextInt(16)));
                    }

                    if (seq.toString().equals(TARGET)) {
                        return "Поток " + threadNum + " нашел: " + seq;
                    }
                }
                return null;
            }));
        }

        // Проверяем результаты
        boolean found = false;
        for (Future<String> future : futures) {
            String result = future.get();
            if (result != null) {
                System.out.println(result);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Не найдено!");
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Всего комбинаций: " + String.format("%,d", totalCombinations));
        System.out.println("Потоков: " + threads);
        System.out.println("или: " + (elapsedTime / 1000.0) + " сек");
    }
}