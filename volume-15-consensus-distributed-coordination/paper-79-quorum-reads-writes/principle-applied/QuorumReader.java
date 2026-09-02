import java.util.List;

// Queries R replicas and returns the one with the HIGHEST version among the
// responses -- not just whichever happens to respond first. This is the other
// half of what a quorum read actually requires: the overlap guarantee only
// helps if the client uses it by comparing versions.
class QuorumReader {
    static Replica readLatest(List<Replica> queriedReplicas) {
        if (queriedReplicas.isEmpty()) {
            throw new IllegalArgumentException("readLatest() requires at least one queried replica");
        }
        Replica latest = queriedReplicas.get(0);
        for (Replica r : queriedReplicas) {
            if (r.version > latest.version) {
                latest = r;
            }
        }
        return latest;
    }
}
