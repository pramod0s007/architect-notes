import java.util.HashMap;
import java.util.Map;

/**
 * PRESSURE: a rebalance reassigns a partition from Consumer A to Consumer B while A is
 * still mid-batch and hasn't learned it was revoked. A keeps processing (and later
 * commits) as if nothing happened; meanwhile B independently starts from the last
 * committed offset. Neither consumer has any way to know the other is also working
 * the same messages, so messages 2, 3, and 4 both get fully processed twice.
 */
public class Main {
    public static void main(String[] args) {
        Partition partition = new Partition(2); // last safe commit before the rebalance
        Map<Integer, Integer> processCounts = new HashMap<>();

        Consumer consumerA = new Consumer("A", processCounts);
        System.out.println("--- Consumer A continues processing messages 2..4, unaware it was revoked ---");
        consumerA.process(2);
        consumerA.process(3);
        consumerA.process(4);

        Consumer consumerB = new Consumer("B", processCounts);
        System.out.println("--- Consumer B starts from committed offset 2 -- rebalance already reassigned the partition to it ---");
        consumerB.process(2);
        consumerB.process(3);
        consumerB.process(4);

        System.out.println("--- Consumer A finally commits -- naive partition blindly accepts it ---");
        partition.commit(5);

        System.out.println();
        System.out.println("Final committed offset: " + partition.getCommittedOffset());
        for (int i = 0; i <= 4; i++) {
            System.out.println("Message " + i + " processed " + processCounts.getOrDefault(i, 0) + " time(s)");
        }
    }
}
