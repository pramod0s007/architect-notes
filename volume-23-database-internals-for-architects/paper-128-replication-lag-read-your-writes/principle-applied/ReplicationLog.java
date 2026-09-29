import java.util.ArrayList;
import java.util.List;

/**
 * Models asynchronous replication explicitly: each write is queued
 * with the simulated "tick" at which it becomes visible on the
 * replica. Ticks are a deterministic stand-in for wall-clock time, not
 * real threads or sleeps -- this lets the same write carry a short lag
 * in one case and a long lag in another, without relying on real
 * timing (which would make the demonstration flaky).
 */
public class ReplicationLog {
    public static class Entry {
        final int version;
        final String key;
        final String value;
        final int arrivalTick;

        Entry(int version, String key, String value, int arrivalTick) {
            this.version = version;
            this.key = key;
            this.value = value;
            this.arrivalTick = arrivalTick;
        }
    }

    private final List<Entry> pending = new ArrayList<>();

    public void enqueue(int version, String key, String value, int lagTicks, int writtenAtTick) {
        pending.add(new Entry(version, key, value, writtenAtTick + lagTicks));
    }

    /** Applies (and removes) every queued entry whose arrival tick has passed. */
    public List<Entry> drainReady(int currentTick) {
        List<Entry> ready = new ArrayList<>();
        List<Entry> stillPending = new ArrayList<>();
        for (Entry e : pending) {
            if (e.arrivalTick <= currentTick) {
                ready.add(e);
            } else {
                stillPending.add(e);
            }
        }
        pending.clear();
        pending.addAll(stillPending);
        return ready;
    }
}
