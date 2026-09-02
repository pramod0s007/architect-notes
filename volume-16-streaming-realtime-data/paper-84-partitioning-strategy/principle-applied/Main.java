import java.util.ArrayList;
import java.util.List;

public class Main {
    static final int NUM_PARTITIONS = 3;
    static final int MS_PER_EVENT = 1;

    public static void main(String[] args) {
        // Same 30 events, same total volume -- partitioned by userId instead of
        // region. userId is high-cardinality (30 distinct values here, millions
        // in a real system) and uniformly distributed, and it's still the RIGHT
        // key: all of one user's events still land on the same partition,
        // preserving per-user ordering exactly like region did for per-region
        // ordering.
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            events.add(new Event("user-" + i, "order-" + i));
        }

        int[] partitionLoad = new int[NUM_PARTITIONS];
        for (Event e : events) {
            int partition = Math.floorMod(e.partitionKey.hashCode(), NUM_PARTITIONS);
            partitionLoad[partition]++;
        }

        System.out.println("30 events, partitioned by userId (30 distinct users, 3 partitions):");
        int maxLoad = 0;
        for (int p = 0; p < NUM_PARTITIONS; p++) {
            System.out.println("  Partition " + p + ": " + partitionLoad[p] + " events, consumer processing time="
                + (partitionLoad[p] * MS_PER_EVENT) + "ms");
            maxLoad = Math.max(maxLoad, partitionLoad[p]);
        }

        int bottleneckMs = maxLoad * MS_PER_EVENT;
        int pressureBottleneckMs = 24;
        System.out.println();
        System.out.println("Total wall-clock time: " + bottleneckMs + "ms");
        System.out.println("vs. region-keyed: " + pressureBottleneckMs + "ms -- "
            + String.format("%.1f", pressureBottleneckMs / (double) bottleneckMs) + "x faster, identical volume.");

        System.out.println(maxLoad <= 10
            ? "FIXED: load is evenly spread across all 3 partitions. Each user's events still all land "
              + "on the same partition -- ordering per user is exactly as strict as ordering per region "
              + "was -- the only thing that changed is which field decides the partition."
            : "Unexpected -- investigate.");
    }
}
