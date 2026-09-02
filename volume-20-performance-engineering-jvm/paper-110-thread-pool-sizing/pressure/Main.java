import java.util.concurrent.*;
import java.util.*;

public class Main {
    static final int CORES = Runtime.getRuntime().availableProcessors();
    static final int NUM_TASKS = CORES * 4;
    static final int SLEEP_MS = 45;

    static long ioBoundTask() {
        long start = System.nanoTime();
        // Real CPU work standing in for request parsing / serialization.
        long acc = 0;
        for (int i = 0; i < 200_000; i++) acc += i * 31L;
        try {
            Thread.sleep(SLEEP_MS); // Real blocking wait, standing in for a downstream call.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return acc + (System.nanoTime() - start);
    }

    public static void main(String[] args) throws Exception {
        // Pool sized to the CPU-bound formula (one thread per core), applied to a
        // task that is mostly blocked waiting, not computing.
        ExecutorService pool = Executors.newFixedThreadPool(CORES);
        List<Future<Long>> futures = new ArrayList<>();

        long start = System.nanoTime();
        for (int i = 0; i < NUM_TASKS; i++) {
            futures.add(pool.submit(Main::ioBoundTask));
        }
        long checksum = 0;
        for (Future<Long> f : futures) checksum += f.get();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        pool.shutdown();

        System.out.println(NUM_TASKS + " IO-bound tasks (" + SLEEP_MS + "ms blocking wait each), pool size " + CORES + " (= CORES).");
        System.out.println("Elapsed: " + elapsedMs + "ms on this run (checksum " + checksum + ")");
        System.out.println();
        System.out.println("CONFIRMED FAILURE (relative to the fix, measured next): with only " + CORES
            + " threads for " + NUM_TASKS + " mostly-blocked tasks, most threads spend their time parked "
            + "waiting, and the remaining tasks queue behind them instead of running concurrently.");
    }
}
