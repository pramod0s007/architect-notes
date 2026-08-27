import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class SizedPool {
    // Goetz's formula gives the ceiling worth considering; cap it at expected
    // concurrent load and what the downstream can actually absorb.
    static int sizeFor(double waitToComputeRatio, int expectedConcurrency) {
        int cores = Runtime.getRuntime().availableProcessors();
        int formulaSize = (int) (cores * (1 + waitToComputeRatio));
        return Math.min(formulaSize, expectedConcurrency);
    }

    static long runBatch(int callCount, long ioWaitMs, int poolSize) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Future<?>> futures = new ArrayList<>();

        long start = System.nanoTime();
        for (int i = 0; i < callCount; i++) {
            futures.add(pool.submit(() -> {
                try {
                    Thread.sleep(ioWaitMs);
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
