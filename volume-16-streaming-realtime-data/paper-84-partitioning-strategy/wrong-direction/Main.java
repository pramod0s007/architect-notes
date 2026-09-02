import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static final int NUM_PARTITIONS = 3;
    // Consumers run independently and are not the same speed in real systems --
    // here partition 2's consumer is 3x slower than 0's (heavier per-event work,
    // slower host, doesn't matter which -- the point is speeds legitimately differ).
    static final int[] MS_PER_EVENT = {1, 1, 3};

    static class Processed {
        final int tick;
        final String key;
        final String payload;
        Processed(int tick, String key, String payload) {
            this.tick = tick;
            this.key = key;
            this.payload = payload;
        }
    }

    public static void main(String[] args) {
        // Round-robin partitioning: assign by SEND POSITION, ignoring the
        // business key entirely. This guarantees perfect load balance -- but
        // two events for the same key can land on two different partitions.
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            events.add(new Event("user-" + i, "evt-" + i));
        }
        // Same user, two sequential status updates: update1 sent first, update2 sent second.
        events.set(5, new Event("user-42", "SHIPPED"));   // update1 (sent first)
        events.set(6, new Event("user-42", "CANCELLED")); // update2 (sent second, should win)

        List<List<Event>> partitions = new ArrayList<>();
        for (int p = 0; p < NUM_PARTITIONS; p++) partitions.add(new ArrayList<>());

        int[] positionOf5 = new int[2]; // [partition, queueIndex] for update1
        int[] positionOf6 = new int[2]; // [partition, queueIndex] for update2
        for (int position = 0; position < events.size(); position++) {
            int partition = position % NUM_PARTITIONS;
            List<Event> queue = partitions.get(partition);
            if (position == 5) { positionOf5[0] = partition; positionOf5[1] = queue.size(); }
            if (position == 6) { positionOf6[0] = partition; positionOf6[1] = queue.size(); }
            queue.add(events.get(position));
        }

        System.out.println("30 events, partitioned round-robin by send position (key ignored entirely):");
        for (int p = 0; p < NUM_PARTITIONS; p++) {
            System.out.println("  Partition " + p + ": " + partitions.get(p).size() + " events");
        }
        System.out.println("Load balance is perfect -- exactly 10/10/10, better than either prior scheme.");
        System.out.println();

        // Each partition's consumer processes its own queue in order, at its own speed.
        List<Processed> processedInGlobalTime = new ArrayList<>();
        for (int p = 0; p < NUM_PARTITIONS; p++) {
            List<Event> queue = partitions.get(p);
            for (int j = 0; j < queue.size(); j++) {
                int tick = (j + 1) * MS_PER_EVENT[p];
                Event e = queue.get(j);
                processedInGlobalTime.add(new Processed(tick, e.partitionKey, e.payload));
            }
        }
        // Sort by the tick each event actually finished processing at (global wall-clock order).
        processedInGlobalTime.sort((a, b) -> Integer.compare(a.tick, b.tick));

        int update1Tick = (positionOf5[1] + 1) * MS_PER_EVENT[positionOf5[0]];
        int update2Tick = (positionOf6[1] + 1) * MS_PER_EVENT[positionOf6[0]];
        System.out.println("update1 (SHIPPED, sent 1st)   -> partition " + positionOf5[0]
            + ", queue index " + positionOf5[1] + ", processed at tick " + update1Tick + "ms");
        System.out.println("update2 (CANCELLED, sent 2nd) -> partition " + positionOf6[0]
            + ", queue index " + positionOf6[1] + ", processed at tick " + update2Tick + "ms");
        System.out.println(update2Tick < update1Tick
            ? "update2 was processed BEFORE update1, despite being sent after it."
            : "Unexpected -- investigate.");
        System.out.println();

        // Apply every update to shared state in the order it was actually PROCESSED, not sent.
        Map<String, String> state = new HashMap<>();
        for (Processed p : processedInGlobalTime) {
            state.put(p.key, p.payload);
        }

        String finalStatus = state.get("user-42");
        System.out.println("Final recorded status for user-42: " + finalStatus);
        System.out.println("CANCELLED".equals(finalStatus)
            ? "Unexpected -- investigate."
            : "CONFIRMED FAILURE: final status is \"" + finalStatus + "\", but update2 (CANCELLED) was "
              + "sent chronologically after update1 (SHIPPED) and should have won. Round-robin split "
              + "this one user's two updates across two partitions with different consumer speeds, so "
              + "the slower partition's update1 was applied LAST, silently overwriting the correct, "
              + "more recent CANCELLED status. The load is perfectly balanced and the bug is invisible "
              + "in every metric that only counts events per partition.");
    }
}
