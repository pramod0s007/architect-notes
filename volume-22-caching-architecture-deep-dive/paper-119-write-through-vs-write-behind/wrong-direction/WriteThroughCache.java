/**
 * A write-through cache: every write goes to the backend synchronously,
 * as part of the same call that accepts it, before being acknowledged.
 * This eliminates the durability gap write-behind has -- there is no
 * buffered state that only exists in memory -- but it means every write
 * now pays the full cost of a backend round trip, not just the ones a
 * scheduled flush happens to batch together.
 */
public class WriteThroughCache {
    private final Backend backend;
    private final int backendWriteCostTicks;
    private int ticksSpent = 0;

    public WriteThroughCache(Backend backend, int backendWriteCostTicks) {
        this.backend = backend;
        this.backendWriteCostTicks = backendWriteCostTicks;
    }

    /** Writes synchronously to the backend before returning -- fully durable when this returns. */
    public void write(String key, String value) {
        backend.write(key, value);
        ticksSpent += backendWriteCostTicks;
    }

    public int ticksSpent() {
        return ticksSpent;
    }
}
