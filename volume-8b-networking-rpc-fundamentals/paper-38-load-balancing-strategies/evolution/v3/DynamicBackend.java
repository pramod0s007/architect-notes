/**
 * A backend whose real capacity can change at runtime (e.g. a GC pause, a slow downstream
 * dependency) — unlike the static {@code Backend} in v1/v2, {@code currentCapacity} is mutable
 * and {@code outstanding} tracks live, unprocessed queue depth.
 */
public class DynamicBackend {
    final int id;
    final int originalCapacity;
    int currentCapacity;
    int outstanding;
    int completed;

    DynamicBackend(int id, int capacity) {
        this.id = id;
        this.originalCapacity = capacity;
        this.currentCapacity = capacity;
    }

    void enqueue(int n) {
        outstanding += n;
    }

    /** Drains up to currentCapacity requests from the queue this tick. */
    void processOneTick() {
        int done = Math.min(outstanding, currentCapacity);
        outstanding -= done;
        completed += done;
    }
}
