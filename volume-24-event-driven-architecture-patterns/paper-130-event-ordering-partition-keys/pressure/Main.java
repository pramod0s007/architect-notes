import java.util.HashMap;
import java.util.Map;

/**
 * PRESSURE: two events for the SAME customer are round-robin-dispatched to two workers
 * with different processing speeds. The slower worker is handed the earlier event; the
 * faster worker is handed the later event. Because completion order depends on worker
 * speed, not production order, the final recorded state is wrong.
 */
public class Main {
    public static void main(String[] args) {
        Map<String, String> entityState = new HashMap<>();

        // Worker A is slow (3 ticks/event), Worker B is fast (1 tick/event).
        Worker workerA = new Worker("WorkerA", 3, entityState);
        Worker workerB = new Worker("WorkerB", 1, entityState);

        // customer-7's events, produced in the correct order: CREATED then CANCELLED.
        Event created = new Event("customer-7", "CREATED", 0);
        Event cancelled = new Event("customer-7", "CANCELLED", 1);

        // Round-robin dispatch, with no regard for entityId: event 0 -> workerA, event 1 -> workerB.
        System.out.println("Dispatch: " + created + " -> WorkerA (slow, 3 ticks/event)");
        System.out.println("Dispatch: " + cancelled + " -> WorkerB (fast, 1 tick/event)");
        workerA.enqueue(created);
        workerB.enqueue(cancelled);

        int tick = 0;
        while (!workerA.isIdle() || !workerB.isIdle()) {
            workerA.tick(tick);
            workerB.tick(tick);
            tick++;
        }

        String finalState = entityState.get("customer-7");
        System.out.println("Final state for customer-7: " + finalState);
        System.out.println("Expected (production order preserved): CANCELLED");
        System.out.println("Order preserved correctly: " + finalState.equals("CANCELLED"));
    }
}
