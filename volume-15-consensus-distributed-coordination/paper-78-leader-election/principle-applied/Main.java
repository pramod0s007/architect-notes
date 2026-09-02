import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Cluster cluster = new Cluster(5);

        System.out.println("Same 5-node cluster, same partition: {N1,N2} and {N3,N4,N5}.");
        System.out.println("A candidate becomes leader only with votes from a MAJORITY of all 5 nodes (3+).");

        // Partition A: N1 can only request votes from N1 and N2 -- 2 reachable voters.
        List<String> partitionAVoters = Arrays.asList("N1", "N2");
        boolean n1CanLead = cluster.canBecomeLeader(partitionAVoters);

        // Partition B: N3 can request votes from N3, N4, N5 -- 3 reachable voters.
        List<String> partitionBVoters = Arrays.asList("N3", "N4", "N5");
        boolean n3CanLead = cluster.canBecomeLeader(partitionBVoters);

        System.out.println("N1's partition: " + partitionAVoters.size() + " reachable voters -- becomes leader: " + n1CanLead);
        System.out.println("N3's partition: " + partitionBVoters.size() + " reachable voters -- becomes leader: " + n3CanLead);

        int leaderCount = (n1CanLead ? 1 : 0) + (n3CanLead ? 1 : 0);
        System.out.println();
        System.out.println("Leaders elected across the entire cluster: " + leaderCount);
        System.out.println((leaderCount == 1 && n3CanLead && !n1CanLead)
            ? "FIXED: exactly one leader, in the majority partition. The minority partition correctly "
              + "has NO leader at all -- it can't accept writes, which is the correct, safe outcome, "
              + "not a failure. No two leaders can ever coexist, because two majorities of the same "
              + "5-node cluster are mathematically guaranteed to share at least one node."
            : "Unexpected -- investigate.");
    }
}
