import java.util.HashMap;
import java.util.Map;

/**
 * PRINCIPLE APPLIED: partition by the event's entityId, not by sequence number and not
 * round-robin. Every event for the same entity always hashes to the same worker, so it's
 * always processed strictly in the order that worker received it (FIFO). Different
 * entities can still land on different workers and process in parallel at different
 * speeds — cross-entity ordering was never required, only per-entity ordering.
 */
public class Main {

    private static int partitionForEntity(String entityId, int numWorkers) {
        return Math.floorMod(entityId.hashCode(), numWorkers);
    }

    public static void main(String[] args) {
        Map<String, String> entityState = new HashMap<>();

        Worker workerA = new Worker("WorkerA", 3, entityState);
        Worker workerB = new Worker("WorkerB", 1, entityState);
        Worker[] workers = { workerA, workerB };

        // customer-7's events: both partitioned by entityId, so both always land on the same worker.
        Event created7 = new Event("customer-7", "CREATED", 0);
        Event cancelled7 = new Event("customer-7", "CANCELLED", 1);

        // customer-8's events: a different entity, may land on a different worker, runs in parallel.
        Event created8 = new Event("customer-8", "CREATED", 2);
        Event shipped8 = new Event("customer-8", "SHIPPED", 3);

        int p7 = partitionForEntity("customer-7", workers.length);
        int p8 = partitionForEntity("customer-8", workers.length);

        System.out.println("customer-7 -> partition " + p7 + " (hash of entityId, both its events land here)");
        System.out.println("customer-8 -> partition " + p8 + " (hash of entityId, both its events land here)");

        workers[p7].enqueue(created7);
        workers[p7].enqueue(cancelled7);
        workers[p8].enqueue(created8);
        workers[p8].enqueue(shipped8);

        int tick = 0;
        while (!workerA.isIdle() || !workerB.isIdle()) {
            workerA.tick(tick);
            workerB.tick(tick);
            tick++;
        }

        String finalState7 = entityState.get("customer-7");
        String finalState8 = entityState.get("customer-8");
        System.out.println("Final state for customer-7: " + finalState7 + " (expected CANCELLED)");
        System.out.println("Final state for customer-8: " + finalState8 + " (expected SHIPPED)");
        boolean bothCorrect = finalState7.equals("CANCELLED") && finalState8.equals("SHIPPED");
        System.out.println("Both entities' per-entity order preserved: " + bothCorrect);
        System.out.println("Same-partition assignment holds regardless of worker speed, because");
        System.out.println("each worker processes its own queue strictly in the order it received it.");
    }
}
