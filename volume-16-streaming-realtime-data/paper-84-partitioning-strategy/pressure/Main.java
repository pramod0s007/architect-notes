import java.util.ArrayList;
import java.util.List;

public class Main {
    static final int NUM_PARTITIONS = 3;
    static final int MS_PER_EVENT = 1; // illustrative fixed per-event processing cost

    public static void main(String[] args) {
        // 30 events, partitioned by "region" -- a real, meaningful business key,
        // but a low-cardinality one: 80% of real traffic comes from one region.
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 24; i++) events.add(new Event("region-A", "order-" + i));
        for (int i = 0; i < 3; i++) events.add(new Event("region-B", "order-" + i));
        for (int i = 0; i < 3; i++) events.add(new Event("region-C", "order-" + i));

        int[] partitionLoad = new int[NUM_PARTITIONS];
        for (Event e : events) {
            int partition = Math.floorMod(e.partitionKey.hashCode(), NUM_PARTITIONS);
            partitionLoad[partition]++;
        }

        System.out.println("30 events, partitioned by region (3 distinct regions, 3 partitions):");
        int maxLoad = 0;
        for (int p = 0; p < NUM_PARTITIONS; p++) {
            System.out.println("  Partition " + p + ": " + partitionLoad[p] + " events, consumer processing time="
                + (partitionLoad[p] * MS_PER_EVENT) + "ms");
            maxLoad = Math.max(maxLoad, partitionLoad[p]);
        }

        int bottleneckMs = maxLoad * MS_PER_EVENT;
        System.out.println();
        System.out.println("Total wall-clock time (bottlenecked by the slowest consumer): " + bottleneckMs + "ms");
        System.out.println(maxLoad > 10
            ? "CONFIRMED FAILURE: one partition carries " + maxLoad + " of 30 events -- its consumer is "
              + "the bottleneck for the entire stream, while the other consumers sit mostly idle. The key "
              + "is a real, meaningful business field; it's just low-cardinality and skewed, which any "
              + "hash-based partitioner turns directly into an uneven partition load."
            : "Unexpected -- investigate.");
    }
}
