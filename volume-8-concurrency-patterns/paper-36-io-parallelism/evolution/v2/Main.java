import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Exception {
        int callCount = 400; // 50 requests x 8 downstream calls each
        long ioWaitMs = 50;  // simulated network round-trip per call

        long elapsedSmallPool = timeBatch(callCount, ioWaitMs, 8);
        long elapsedLargePool = timeBatch(callCount, ioWaitMs, 64);

        System.out.println("Pool size 8:  " + elapsedSmallPool + "ms");
        System.out.println("Pool size 64: " + elapsedLargePool + "ms");
        System.out.println("v2: same work, same hardware, sizing alone changed throughput this much.");
    }

    private static long timeBatch(int callCount, long ioWaitMs, int poolSize) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Future<?>> futures = new ArrayList<>();

        long start = System.nanoTime();
        for (int i = 0; i < callCount; i++) {
            futures.add(pool.submit(() -> {
                try {
                    Thread.sleep(ioWaitMs); // stands in for a blocking network round-trip
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        for (Future<?> f : futures) f.get();
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        pool.shutdown();
        return elapsed;
    }
}
