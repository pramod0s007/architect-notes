import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Node n1 = new Node("N1");
        Node n2 = new Node("N2");
        Node n3 = new Node("N3");
        Node n4 = new Node("N4");
        Node n5 = new Node("N5");

        System.out.println("5-node cluster. Network partition splits it into {N1,N2} and {N3,N4,N5} -- "
            + "neither side can reach the other.");
        System.out.println("Naive election: whichever node in each partition notices no leader first, "
            + "declares itself leader to whoever it CAN reach. No majority check.");

        // Partition A: N1 declares itself leader to the nodes it can still reach (N2).
        n1.believesItIsLeader = true;
        // Partition B: N3 declares itself leader to the nodes it can still reach (N4, N5).
        n3.believesItIsLeader = true;

        int leaderCount = 0;
        for (Node n : new Node[]{n1, n2, n3, n4, n5}) {
            if (n.believesItIsLeader) {
                leaderCount++;
                System.out.println("  " + n.id + " believes it is the leader.");
            }
        }

        // Both "leaders" accept a write for the same key, unaware of each other.
        Map<String, Integer> partitionAStore = new HashMap<>();
        Map<String, Integer> partitionBStore = new HashMap<>();
        partitionAStore.put("balance", 100);   // client wrote to N1's partition
        partitionBStore.put("balance", 250);   // a DIFFERENT client wrote to N3's partition

        System.out.println();
        System.out.println("Leaders active simultaneously: " + leaderCount);
        System.out.println("Partition A's recorded balance: " + partitionAStore.get("balance"));
        System.out.println("Partition B's recorded balance: " + partitionBStore.get("balance"));
        System.out.println(leaderCount > 1
            ? "CONFIRMED FAILURE: two nodes simultaneously believe they are the leader. Each accepted "
              + "a write for the same key, and the two values disagree -- 100 vs 250 -- with no way, "
              + "once the partition heals, to know which one was ever actually authoritative."
            : "Unexpected -- investigate.");
    }
}
