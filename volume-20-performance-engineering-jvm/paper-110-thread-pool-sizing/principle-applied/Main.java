import java.util.concurrent.*;
import java.util.*;

public class Main {
    static final int CORES = Runtime.getRuntime().availableProcessors();
    static final int NUM_TASKS = CORES * 4;
    static final int SLEEP_MS = 45;
    // Sized for a mostly-blocked workload: cores * (1 + wait/compute), not cores alone.
    static final int POOL_SIZE = CORES * 10;

    static long ioBoundTask() {
        long start = System.nanoTime();
        long acc = 0;
        for (int i = 0; i < 200_000; i++) acc += i * 31L;
        try {
            Thread.sleep(SLEEP_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return acc + (System.nanoTime() - start);
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
        List<Future<Long>> futures = new ArrayList<>();

        long start = System.nanoTime();
        for (int i = 0; i < NUM_TASKS; i++) {
            futures.add(pool.submit(Main::ioBoundTask));
        }
        long checksum = 0;
        for (Future<Long> f : futures) checksum += f.get();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        pool.shutdown();

        System.out.println(NUM_TASKS + " IO-bound tasks (" + SLEEP_MS + "ms blocking wait each), pool size " + POOL_SIZE + " (= CORES * 10).");
        System.out.println("Elapsed: " + elapsedMs + "ms on this run (checksum " + checksum + ")");
        System.out.println();
        System.out.println("FIXED: sized for how much time each task spends blocked rather than computing, "
            + "the pool now has enough threads for essentially all " + NUM_TASKS + " tasks to be in flight "
            + "at once, so the wall-clock time collapses toward the cost of one task, not " + NUM_TASKS + ".");
    }
}
