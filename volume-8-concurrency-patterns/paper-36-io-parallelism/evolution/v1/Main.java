import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Exception {
        int poolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService cpuBoundPool = Executors.newFixedThreadPool(poolSize);

        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futures.add(cpuBoundPool.submit(Main::sumOfSquares));
        }
        long total = 0;
        for (Future<Long> f : futures) total += f.get();

        cpuBoundPool.shutdown();
        System.out.println("Pool size " + poolSize + " (== core count). Result: " + total);
        System.out.println("v1: CPU-bound work, no blocking inside the task. Pool size = cores is correct here.");
    }

    private static long sumOfSquares() {
        long sum = 0;
        for (int i = 0; i < 5_000_000; i++) sum += (long) i * i;
        return sum;
    }
}
