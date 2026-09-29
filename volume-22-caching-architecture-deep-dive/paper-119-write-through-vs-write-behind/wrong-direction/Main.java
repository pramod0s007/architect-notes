/**
 * The team's fix after the data-loss incident: switch to write-through,
 * so every write is durable in the backend before it's ever acknowledged.
 * This genuinely eliminates the durability gap -- there is nothing left
 * to lose on a crash, because nothing is ever only-in-memory. But it
 * introduces a different, equally real cost: every write now pays a
 * synchronous backend round trip, and under a burst of writes, that
 * round-trip cost adds up fast enough to blow through the system's
 * actual latency budget.
 */
public class Main {
    private static final int WRITE_COUNT = 30;
    private static final int BACKEND_WRITE_COST_TICKS = 5;
    private static final int TICK_BUDGET = 100; // the burst must finish processing within this many ticks

    public static void main(String[] args) {
        Backend backend = new Backend();
        WriteThroughCache cache = new WriteThroughCache(backend, BACKEND_WRITE_COST_TICKS);

        System.out.println("=== " + WRITE_COUNT + " writes, each costing " + BACKEND_WRITE_COST_TICKS
                + " ticks for a synchronous backend round trip ===");
        for (int i = 0; i < WRITE_COUNT; i++) {
            cache.write("key-" + i, "value-" + i);
        }

        System.out.println("Writes durable in backend: " + backend.writeCount() + " (0 lost, even under crash)");
        System.out.println("Total ticks spent processing the burst: " + cache.ticksSpent());
        System.out.println("Tick budget for this burst: " + TICK_BUDGET);

        if (cache.ticksSpent() > TICK_BUDGET) {
            int ticksOverBudget = cache.ticksSpent() - TICK_BUDGET;
            int writesThatFitInBudget = TICK_BUDGET / BACKEND_WRITE_COST_TICKS;
            System.out.println();
            System.out.println("CONFIRMED WRONG DIRECTION: write-through genuinely eliminated the data-loss "
                    + "failure -- all " + WRITE_COUNT + " writes are durable, none lost, no matter when a crash "
                    + "happens. But processing the burst cost " + cache.ticksSpent() + " ticks against a budget "
                    + "of " + TICK_BUDGET + " -- " + ticksOverBudget + " ticks over. Only " + writesThatFitInBudget
                    + " of the " + WRITE_COUNT + " writes would actually complete within the budget; the rest "
                    + "queue up and miss the system's own latency requirement, a failure durability alone "
                    + "doesn't fix.");
        }
    }
}
