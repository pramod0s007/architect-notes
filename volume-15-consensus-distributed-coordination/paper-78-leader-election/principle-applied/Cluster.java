import java.util.List;

// A candidate becomes leader only if it receives votes from a STRICT MAJORITY
// of the entire cluster -- not just the nodes it can currently reach. Because
// any two majorities of the same set must overlap by at least one node, this
// mathematically guarantees at most one leader can be elected in a given term,
// no matter how the network is partitioned.
class Cluster {
    private final int totalNodes;

    Cluster(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    boolean canBecomeLeader(List<String> reachableVoters) {
        int majority = totalNodes / 2 + 1;
        return reachableVoters.size() >= majority;
    }
}
