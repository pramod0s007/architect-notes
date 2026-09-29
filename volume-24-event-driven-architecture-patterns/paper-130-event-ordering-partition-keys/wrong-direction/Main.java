import java.util.HashMap;
import java.util.Map;

/**
 * WRONG DIRECTION: instead of round-robin, dispatch by hashing the event's OWN id
 * (its sequence number) rather than the entity it belongs to. This "looks" like a
 * smarter routing rule than plain round-robin, but it still has no relationship to
 * which entity the event is about — two events for the same customer can still land
 * on different workers, and the bug still reproduces.
 */
public class Main {

    // Hashes on the event's sequence number, NOT on entityId — the actual mistake.
    private static int partitionForEvent(Event e, int numWorkers) {
        return Math.floorMod(Integer.hashCode(e.getSequenceNum()), numWorkers);
    }

    public static void main(String[] args) {
        Map<String, String> entityState = new HashMap<>();

        Worker workerA = new Worker("WorkerA", 3, entityState);
        Worker workerB = new Worker("WorkerB", 1, entityState);
        Worker[] workers = { workerA, workerB };

        Event created = new Event("customer-7", "CREATED", 0);
        Event cancelled = new Event("customer-7", "CANCELLED", 1);

        int partitionForCreated = partitionForEvent(created, workers.length);
        int partitionForCancelled = partitionForEvent(cancelled, workers.length);

        System.out.println("Dispatch: " + created + " -> partition " + partitionForCreated
                + " (hash of sequenceNum=0)");
        System.out.println("Dispatch: " + cancelled + " -> partition " + partitionForCancelled
                + " (hash of sequenceNum=1)");
        workers[partitionForCreated].enqueue(created);
        workers[partitionForCancelled].enqueue(cancelled);

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
        System.out.println();
        System.out.println("Hashing on the event's own id 'looks' deterministic and well-engineered,");
        System.out.println("but it still routes same-entity events to different workers whenever");
        System.out.println("their sequence numbers happen to hash to different partitions.");
    }
}
