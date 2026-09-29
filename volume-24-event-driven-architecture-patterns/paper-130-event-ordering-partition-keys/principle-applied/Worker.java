import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * A single worker with its own FIFO queue and a fixed per-event processing cost in ticks.
 * Workers run independently and at different speeds, deterministically, one tick at a time
 * (no real threads — this models concurrency without relying on real thread interleaving).
 */
public class Worker {
    private final String name;
    private final int ticksPerEvent;
    private final Deque<Event> queue = new ArrayDeque<>();
    private final Map<String, String> entityState;
    private int busyUntilTick = -1;
    private Event inFlight;

    public Worker(String name, int ticksPerEvent, Map<String, String> sharedEntityState) {
        this.name = name;
        this.ticksPerEvent = ticksPerEvent;
        this.entityState = sharedEntityState;
    }

    public void enqueue(Event e) {
        queue.add(e);
    }

    /** Advance this worker by one tick. If it finishes processing, it writes the entity's state. */
    public void tick(int currentTick) {
        if (inFlight == null && !queue.isEmpty()) {
            inFlight = queue.poll();
            busyUntilTick = currentTick + ticksPerEvent;
        }
        if (inFlight != null && currentTick >= busyUntilTick) {
            entityState.put(inFlight.getEntityId(), inFlight.getType());
            System.out.println("[tick " + currentTick + "] " + name + " finished " + inFlight
                    + " -> state[" + inFlight.getEntityId() + "]=" + inFlight.getType());
            inFlight = null;
        }
    }

    public boolean isIdle() {
        return inFlight == null && queue.isEmpty();
    }
}
