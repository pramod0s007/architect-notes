import java.util.HashMap;
import java.util.Map;

/**
 * PRINCIPLE APPLIED: epoch (generation) fencing. Every rebalance bumps the partition's
 * epoch. Consumer A still believes it holds the epoch from before the rebalance, so when
 * it eventually tries to commit, the partition rejects it as stale -- A can no longer
 * corrupt the committed offset, whether it finished its whole batch or not. Consumer B,
 * which holds the current epoch, commits normally. The committed offset always reflects
 * real, confirmed progress: no message is ever skipped, unlike the wrong-direction
 * design's optimistic commit.
 */
public class Main {
    public static void main(String[] args) {
        Partition partition = new Partition(2, 1); // last safe commit before the rebalance, epoch 1

        // Rebalance: partition reassigned from A to B. The coordinator bumps the epoch;
        // A has no way to know this yet -- it is now a "zombie" holding a stale epoch.
        partition.startNewGeneration();

        Map<Integer, Integer> processCounts = new HashMap<>();

        Consumer consumerA = new Consumer("A", processCounts);
        System.out.println("--- Consumer A (stale epoch 1) keeps processing messages 2..4, unaware it was revoked ---");
        consumerA.process(2);
        consumerA.process(3);
        consumerA.process(4);

        System.out.println("--- Consumer A tries to commit under its stale epoch 1 ---");
        boolean acceptedA = partition.commit(5, 1);
        System.out.println("A's commit accepted? " + acceptedA);

        Consumer consumerB = new Consumer("B", processCounts);
        System.out.println("--- Consumer B (current epoch " + partition.getCurrentEpoch() + ") processes messages 2..4 ---");
        consumerB.process(2);
        consumerB.process(3);
        consumerB.process(4);

        System.out.println("--- Consumer B commits under the current epoch ---");
        boolean acceptedB = partition.commit(5, partition.getCurrentEpoch());
        System.out.println("B's commit accepted? " + acceptedB);

        System.out.println();
        System.out.println("Final committed offset: " + partition.getCommittedOffset());
        for (int i = 0; i <= 4; i++) {
            System.out.println("Message " + i + " processed " + processCounts.getOrDefault(i, 0) + " time(s)");
        }
    }
}
