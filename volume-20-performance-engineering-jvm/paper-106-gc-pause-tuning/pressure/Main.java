public class Main {
    // A simplified, honestly-disclosed simulation of old-generation GC
    // mechanics, not a literal JVM GC log: a full GC scans and compacts
    // everything currently in old gen -- the genuinely long-lived working
    // set AND whatever garbage has accumulated since the last collection --
    // so its pause cost scales with total old-gen occupancy, not just with
    // how much garbage there is to reclaim.
    static final int LIVE_WORKING_SET = 50;   // genuinely long-lived objects that must stay resident
    static final int GARBAGE_PER_TICK = 10;   // new old-gen garbage generated per unit of work
    static final int WINDOW_TICKS = 100;      // the observation window

    public static void main(String[] args) {
        int capacity = 70; // barely above the live working set -- only 20 units of headroom
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

        System.out.println("Old gen capacity: " + capacity + " (only 20 units above the live working set of " + LIVE_WORKING_SET + ")");
        System.out.println("Over " + WINDOW_TICKS + " ticks: " + gcCount + " full GCs fired");
        System.out.println("Total pause time: " + totalPauseMs + "ms, max single pause: " + maxPauseMs + "ms");
        System.out.println("GC overhead: " + String.format("%.1f", overheadPercent) + "% of total time");

        System.out.println();
        System.out.println(overheadPercent > 30
            ? "CONFIRMED FAILURE: with barely any headroom above the live working set, garbage never has "
              + "room to accumulate before triggering another full collection. " + gcCount + " GCs fired "
              + "in this window, consuming " + String.format("%.1f", overheadPercent) + "% of total time -- "
              + "throughput collapses under constant collection, even though no single pause is "
              + "individually catastrophic."
            : "Unexpected -- investigate.");
    }
}
