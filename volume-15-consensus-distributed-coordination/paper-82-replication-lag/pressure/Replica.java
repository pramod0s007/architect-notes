// A read replica. replicatedLsn is how far its asynchronous replication
// stream has caught up to the primary's write log -- standing in for real
// replication lag, which is normally a matter of milliseconds but is never
// exactly zero.
class Replica {
    final String id;
    int replicatedLsn;
    private String bio;

    Replica(String id, int replicatedLsn, String bio) {
        this.id = id;
        this.replicatedLsn = replicatedLsn;
        this.bio = bio;
    }

    String readBio() {
        return bio;
    }
}
