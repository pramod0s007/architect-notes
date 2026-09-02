public class Main {
    // A simplified, honestly-disclosed simulation of JIT tiered compilation:
    // a code path runs interpreted (slow) until it's been invoked enough
    // times, then runs compiled (fast). Real JVMs use invocation counters
    // and back-edge counters against tunable thresholds to make this exact
    // decision -- this model captures the shape of that behavior, not a
    // literal HotSpot compilation log.
    static final int COMPILE_THRESHOLD = 1000;
    static final int INTERPRETED_LATENCY_MS = 50;
    static final int COMPILED_LATENCY_MS = 2;

    static long simulatePath(int startingInvocations, int requestCount) {
        long totalMs = 0;
        int invocations = startingInvocations;
        for (int i = 0; i < requestCount; i++) {
            totalMs += (invocations < COMPILE_THRESHOLD) ? INTERPRETED_LATENCY_MS : COMPILED_LATENCY_MS;
            invocations++;
        }
        return totalMs;
    }

    public static void main(String[] args) {
        // Overcorrection: a warmup phase genuinely runs before real traffic
        // -- this isn't the pressure case's mistake. But the synthetic
        // warmup script only exercises Path A, the simpler of the two real
        // code paths, and was never updated to cover Path B, which is
        // actually the majority of real traffic.
        long warmupATotal = simulatePath(0, COMPILE_THRESHOLD);
        System.out.println("Warmup: " + COMPILE_THRESHOLD + " synthetic invocations of Path A only. Path B is not exercised.");
        System.out.println("Readiness gate reports: warmup complete.");

        long pathATotal = simulatePath(COMPILE_THRESHOLD, 600);
        long pathBTotal = simulatePath(0, 1400); // Path B starts real traffic completely cold
        long totalMs = pathATotal + pathBTotal;
        int totalRequests = 600 + 1400;

        System.out.println();
        System.out.println("Path A: 600 real requests, warmed -- total: " + pathATotal + "ms");
        System.out.println("Path B: 1400 real requests, NEVER warmed -- total: " + pathBTotal + "ms");
        System.out.println("Combined: " + totalRequests + " real requests, " + totalMs + "ms total, average "
            + String.format("%.1f", totalMs / (double) totalRequests) + "ms/request");

        System.out.println();
        System.out.println(pathATotal == 1_200 && pathBTotal == 50_800
            ? "CONFIRMED FAILURE: the readiness gate reported warmup complete, and Path A genuinely is "
              + "fast from request one. But Path B -- 70% of real traffic, the majority -- was never "
              + "exercised by the warmup script at all, so it starts exactly as cold as if no warmup had "
              + "run for it. The average latency this instance actually delivers to real users is far "
              + "closer to the no-warmup case than to a correctly warmed one, because 'warmup ran' was "
              + "never the same claim as 'warmup covered what real traffic actually needs.'"
            : "Unexpected -- investigate.");
    }
}
