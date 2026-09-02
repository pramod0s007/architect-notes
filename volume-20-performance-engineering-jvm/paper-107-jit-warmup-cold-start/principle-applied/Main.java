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
        // Before accepting real traffic, a readiness gate drives synthetic
        // requests through EVERY real code path the traffic mix actually
        // uses -- both Path A and Path B -- until each individually clears
        // the compilation threshold.
        long warmupATotal = simulatePath(0, COMPILE_THRESHOLD);
        long warmupBTotal = simulatePath(0, COMPILE_THRESHOLD);
        System.out.println("Warmup: " + COMPILE_THRESHOLD + " synthetic invocations of Path A, "
            + COMPILE_THRESHOLD + " of Path B, before any real traffic is accepted.");
        System.out.println("Warmup cost (paid once, before traffic, not by real users): "
            + (warmupATotal + warmupBTotal) + "ms");

        // Real traffic now starts with both paths already compiled.
        long pathATotal = simulatePath(COMPILE_THRESHOLD, 600);
        long pathBTotal = simulatePath(COMPILE_THRESHOLD, 1400);
        long totalMs = pathATotal + pathBTotal;
        int totalRequests = 600 + 1400;

        System.out.println();
        System.out.println("Path A: 600 real requests, pre-warmed -- total: " + pathATotal + "ms");
        System.out.println("Path B: 1400 real requests, pre-warmed -- total: " + pathBTotal + "ms");
        System.out.println("Combined: " + totalRequests + " real requests, " + totalMs + "ms total, average "
            + String.format("%.1f", totalMs / (double) totalRequests) + "ms/request");

        System.out.println();
        System.out.println(totalMs == 4_000
            ? "FIXED: every real request, on both paths, sees compiled-path latency from the very first "
              + "one. The interpreted cost still happened -- 100000ms of it -- but it was paid once, by "
              + "synthetic warmup traffic, before any real user's request was in flight, instead of being "
              + "spread across the first thousand real users on each path."
            : "Unexpected -- investigate.");
    }
}
