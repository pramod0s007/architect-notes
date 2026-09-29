import java.util.HashMap;
import java.util.Map;

/**
 * WRONG DIRECTION: "fix" the duplicate-processing problem by having a revoked consumer
 * commit optimistically -- as soon as it learns it's being revoked, it commits the
 * offset for its ENTIRE assigned batch, assuming it would have finished. This does stop
 * A and B from double-processing the same messages. But A was revoked before it actually
 * reached message 4, so the committed offset now points past a message nobody ever
 * processed. Consumer B starts from that already-advanced offset and message 4 is gone
 * -- not duplicated, just silently skipped, with no error and no record.
 */
public class Main {
    public static void main(String[] args) {
        Partition partition = new Partition(2); // last safe commit before the rebalance
        Map<Integer, Integer> processCounts = new HashMap<>();

        Consumer consumerA = new Consumer("A", processCounts);
        System.out.println("--- Consumer A processes messages 2..3, then is revoked before reaching message 4 ---");
        consumerA.process(2);
        consumerA.process(3);
        // revoked here -- A never reaches message 4

        System.out.println("--- Consumer A commits optimistic offset 5 on revocation, assuming its whole batch finished ---");
        partition.commit(5);

        Consumer consumerB = new Consumer("B", processCounts);
        System.out.println("--- Consumer B starts from committed offset 5 -- message 4 is never read by anyone ---");
        // Nothing left for B to process in this demo window: the committed offset already
        // points past message 4, so B has no reason to ever request it.

        System.out.println();
        System.out.println("Final committed offset: " + partition.getCommittedOffset());
        for (int i = 0; i <= 4; i++) {
            System.out.println("Message " + i + " processed " + processCounts.getOrDefault(i, 0) + " time(s)");
        }
    }
}
