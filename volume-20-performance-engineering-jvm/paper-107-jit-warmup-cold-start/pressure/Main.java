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
        // A new instance receives full real traffic immediately -- no
        // warmup phase at all. Real traffic is a realistic mix: 600
        // requests on Path A, 1400 on Path B.
        long pathATotal = simulatePath(0, 600);
        long pathBTotal = simulatePath(0, 1400);
        long totalMs = pathATotal + pathBTotal;
        int totalRequests = 600 + 1400;

        System.out.println("Path A: 600 real requests, no warmup -- total: " + pathATotal + "ms");
        System.out.println("Path B: 1400 real requests, no warmup -- total: " + pathBTotal + "ms");
        System.out.println("Combined: " + totalRequests + " real requests, " + totalMs + "ms total, average "
            + String.format("%.1f", totalMs / (double) totalRequests) + "ms/request");

        System.out.println();
        System.out.println(pathATotal == 30_000 && totalMs == 80_800
            ? "CONFIRMED FAILURE: Path A never even reaches the compilation threshold within its own "
              + "600 real requests -- every single one of them runs interpreted, at full cost, because "
              + "the instance was never given a chance to compile before real users started arriving. "
              + "Path B eventually compiles, but only after 1000 real users already paid the interpreted "
              + "latency on its behalf."
            : "Unexpected -- investigate.");
    }
}
