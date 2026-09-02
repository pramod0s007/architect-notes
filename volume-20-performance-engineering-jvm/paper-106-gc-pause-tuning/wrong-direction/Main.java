public class Main {
    static final int LIVE_WORKING_SET = 50;
    static final int GARBAGE_PER_TICK = 10;
    static final int WINDOW_TICKS = 100; // the same short observation window used to judge the fix
    static final int MAX_ACCEPTABLE_PAUSE_MS = 300;

    public static void main(String[] args) {
        // Overcorrection: "just give it far more memory than it could ever
        // need, so GC essentially never happens." This isn't the pressure
        // case's mistake -- there's plenty of headroom, not too little.
        int capacity = 10_050;
        int garbage = 0;
        int gcCount = 0;
        long totalPauseMs = 0;

        for (int tick = 0; tick < WINDOW_TICKS; tick++) {
            garbage += GARBAGE_PER_TICK;
            if (LIVE_WORKING_SET + garbage >= capacity) {
                long pause = LIVE_WORKING_SET + garbage;
                totalPauseMs += pause;
                gcCount++;
                garbage = 0;
            }
        }

        double overheadPercent = (totalPauseMs / (double) (WINDOW_TICKS * 100)) * 100;
        System.out.println("Old gen capacity: " + capacity + " (far more than the workload could plausibly need)");
        System.out.println("Over the same " + WINDOW_TICKS + "-tick observation window used to judge the fix: "
            + gcCount + " full GCs fired, " + String.format("%.1f", overheadPercent) + "% overhead");

        // Keep running the identical workload past the short observation
        // window, until the capacity that was "clearly safe" finally fills.
        int tick = WINDOW_TICKS;
        while (LIVE_WORKING_SET + garbage < capacity) {
            garbage += GARBAGE_PER_TICK;
            tick++;
        }
        long eventualPause = LIVE_WORKING_SET + garbage;

        System.out.println();
        System.out.println("Continuing the identical workload, the first full GC finally fires at tick " + tick + ":");
        System.out.println("Pause when it fires: " + eventualPause + "ms");

        System.out.println();
        System.out.println(gcCount == 0 && eventualPause > MAX_ACCEPTABLE_PAUSE_MS * 10
            ? "CONFIRMED FAILURE: the short observation window shows a perfect result -- zero GCs, zero "
              + "overhead, better than the properly-sized fix on every number visible in that window. But "
              + "the pause was never eliminated, only deferred and inflated: when it finally fires, it "
              + "scans and compacts " + eventualPause + "ms worth of old gen in one stop-the-world pause -- "
              + "over " + (eventualPause / MAX_ACCEPTABLE_PAUSE_MS) + "x the service's own " + MAX_ACCEPTABLE_PAUSE_MS
              + "ms latency SLA, in a single hit. A short load test or monitoring window would have "
              + "recommended this exact configuration as the best of the three."
            : "Unexpected -- investigate.");
    }
}
