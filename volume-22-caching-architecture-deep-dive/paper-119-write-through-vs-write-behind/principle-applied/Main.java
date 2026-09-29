/**
 * The real fix: keep write-behind's speed (writes never pay a synchronous
 * backend round trip), but make each write durable the moment it's
 * accepted by appending it to a cheap, sequential, local write-ahead log
 * -- not by writing all the way through to the backend. A crash before
 * the scheduled flush no longer loses anything, because replaying the
 * durable log recovers every write that was ever accepted, at a fraction
 * of write-through's per-write cost.
 */
public class Main {
    private static final int WRITE_COUNT = 30;
    private static final int LOG_APPEND_COST_TICKS = 1;
    private static final int TICK_BUDGET = 100;

    public static void main(String[] args) {
        Backend backend = new Backend();
        WriteBehindWithWalCache cache = new WriteBehindWithWalCache(backend, LOG_APPEND_COST_TICKS);

        System.out.println("=== " + WRITE_COUNT + " writes, each costing " + LOG_APPEND_COST_TICKS
                + " tick for a durable local log append (not a backend round trip) ===");
        for (int i = 0; i < WRITE_COUNT; i++) {
            cache.write("key-" + i, "value-" + i);
        }
        System.out.println("Writes durable in backend so far: " + backend.writeCount());
        System.out.println("Total ticks spent processing the burst: " + cache.ticksSpent());
        System.out.println("Tick budget for this burst: " + TICK_BUDGET);

        // The process crashes here -- before the scheduled flush ever runs -- exactly like the pressure scenario.
        System.out.println();
        System.out.println("=== Process crashes before the scheduled flush runs ===");
        int recovered = cache.recoverFromCrash();
        System.out.println("Writes recovered by replaying the durable log: " + recovered);
        System.out.println("Writes durable in backend after recovery: " + backend.writeCount());

        if (cache.ticksSpent() <= TICK_BUDGET && backend.writeCount() == WRITE_COUNT) {
            System.out.println();
            System.out.println("FIXED: the burst finished in " + cache.ticksSpent() + " ticks, well within the "
                    + TICK_BUDGET + "-tick budget -- unlike write-through's 150 ticks for the same burst -- "
                    + "because each write only paid the cost of a local log append, not a backend round trip. "
                    + "And unlike plain write-behind, the crash lost nothing: all " + recovered + " buffered "
                    + "writes were recovered by replaying the durable log, leaving all " + WRITE_COUNT
                    + " durable in the backend after recovery, the same guarantee write-through provided, "
                    + "without paying its per-write cost.");
        }
    }
}
