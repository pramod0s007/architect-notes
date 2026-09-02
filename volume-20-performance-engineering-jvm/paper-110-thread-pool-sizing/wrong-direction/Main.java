import java.util.concurrent.*;
import java.util.*;

public class Main {
    static final int CORES = Runtime.getRuntime().availableProcessors();
    static final int NUM_TASKS = CORES * 40;
    static final int OVERSIZED_POOL = CORES * 50;
    static final int TRIALS = 7;

    // Purely CPU-bound: no blocking wait at all, unlike Paper 110's IO-bound task.
    static long cpuBoundTask() {
        long acc = 0;
        for (int i = 0; i < 5_000_000; i++) acc += i * 31L;
        return acc;
    }

    static long checksum = 0;

    static long oneRun(int poolSize) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Future<Long>> futures = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < NUM_TASKS; i++) {
            futures.add(pool.submit(Main::cpuBoundTask));
        }
        for (Future<Long> f : futures) checksum += f.get();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        pool.shutdown();
        return elapsedMs;
    }

    static long median(int poolSize) throws Exception {
        long[] times = new long[TRIALS];
        for (int t = 0; t < TRIALS; t++) times[t] = oneRun(poolSize);
        Arrays.sort(times);
        return times[TRIALS / 2];
    }

    public static void main(String[] args) throws Exception {
        // Warm up the JIT on cpuBoundTask itself before either timed measurement, so
        // neither pool-size comparison is biased by one of them absorbing the one-time
        // cost of the method getting compiled.
        for (int i = 0; i < 4000; i++) cpuBoundTask();

        // Applying the "more threads fixed the last problem" lesson from the IO-bound
        // case to a task that never blocks at all. Each figure is the median of
        // several back-to-back trials, to average out scheduler noise.
        long coresMs = median(CORES);
        long oversizedMs = median(OVERSIZED_POOL);

        System.out.println(NUM_TASKS + " CPU-bound tasks, pool size " + CORES + " (= CORES), median of " + TRIALS + " trials: " + coresMs + "ms");
        System.out.println(NUM_TASKS + " CPU-bound tasks, pool size " + OVERSIZED_POOL + " (= CORES * 50), median of " + TRIALS + " trials: " + oversizedMs + "ms");
        System.out.println("(checksum " + checksum + ", used only to prevent the JIT discarding the work)");
        System.out.println();
        System.out.println(oversizedMs > coresMs
            ? "CONFIRMED FAILURE: the oversized pool that fixed the IO-bound case makes this CPU-bound case "
              + "slower, not faster. There are only " + CORES + " cores to actually run on; giving the OS "
              + OVERSIZED_POOL + " runnable threads to schedule across them adds real context-switching and "
              + "cache-contention overhead with no additional parallelism available to absorb it."
            : "Unexpected -- investigate.");
    }
}
