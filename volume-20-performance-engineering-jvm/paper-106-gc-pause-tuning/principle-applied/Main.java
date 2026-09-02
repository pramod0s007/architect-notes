public class Main {
    static final int LIVE_WORKING_SET = 50;
    static final int GARBAGE_PER_TICK = 10;
    static final int WINDOW_TICKS = 100;
    static final int MAX_ACCEPTABLE_PAUSE_MS = 300; // the actual latency SLA for this service

    public static void main(String[] args) {
        // Sized based on the application's real, profiled working set and
        // garbage generation rate, with headroom chosen to keep every
        // single pause under the service's own latency SLA, not just
        // "as large as possible."
        int capacity = 250;
        int garbage = 0;
        int gcCount = 0;
        long totalPauseMs = 0;
        long maxPauseMs = 0;

        for (int tick = 0; tick < WINDOW_TICKS; tick++) {
            garbage += GARBAGE_PER_TICK;
            if (LIVE_WORKING_SET + garbage >= capacity) {
                long pause = LIVE_WORKING_SET + garbage;
                totalPauseMs += pause;
                maxPauseMs = Math.max(maxPauseMs, pause);
                gcCount++;
                garbage = 0;
            }
        }

        double overheadPercent = (totalPauseMs / (double) (WINDOW_TICKS * 100)) * 100;

        System.out.println("Old gen capacity: " + capacity + " (sized from profiling: live working set + a bounded headroom)");
        System.out.println("Over " + WINDOW_TICKS + " ticks: " + gcCount + " full GCs fired");
        System.out.println("Total pause time: " + totalPauseMs + "ms, max single pause: " + maxPauseMs + "ms");
        System.out.println("GC overhead: " + String.format("%.1f", overheadPercent) + "% of total time");
        System.out.println("Latency SLA (max acceptable single pause): " + MAX_ACCEPTABLE_PAUSE_MS + "ms");

        System.out.println();
        System.out.println(overheadPercent < 15 && maxPauseMs < MAX_ACCEPTABLE_PAUSE_MS
            ? "FIXED: GC overhead dropped from 35% to " + String.format("%.1f", overheadPercent)
              + "% of total time, because garbage now has real room to accumulate before a collection is "
              + "needed -- and the max single pause (" + maxPauseMs + "ms) still stays comfortably under "
              + "the " + MAX_ACCEPTABLE_PAUSE_MS + "ms SLA. Sizing was chosen from the actual working set "
              + "and garbage rate, not a guess in either direction."
            : "Unexpected -- investigate.");
    }
}
