/**
 * Demonstrates read-your-writes violation under asynchronous
 * replication: a write to the primary is queued for the replica with
 * a 3-tick replication lag (simulated via explicit ticks, not real
 * threads or sleeps), and a read routed to the replica immediately
 * afterward -- at tick 0, before the write has had any chance to
 * arrive -- sees stale data even though the write already succeeded
 * on the primary.
 */
public class Main {
    public static void main(String[] args) {
        Primary primary = new Primary();
        Replica replica = new Replica();
        ReplicationLog log = new ReplicationLog();

        int currentTick = 0;
        int version = primary.write("profile:42", "Jane Doe (updated)");
        log.enqueue(version, "profile:42", "Jane Doe (updated)", 3, currentTick);

        // Client writes, then immediately reads its own write back --
        // from the replica, since reads are routed there to keep load
        // off the primary. No time has passed, so the replica has had
        // no chance to receive the write yet.
        String valueOnPrimary = primary.read("profile:42");
        String valueOnReplica = replica.read("profile:42");
        boolean readYourWritesViolated = valueOnReplica == null || !valueOnReplica.equals(valueOnPrimary);

        System.out.println("Wrote to primary at tick " + currentTick + ", version " + version);
        System.out.println("Value on primary immediately after write: " + valueOnPrimary);
        System.out.println("Value on replica immediately after write: " + valueOnReplica);
        System.out.println("Read-your-writes violated: " + readYourWritesViolated);
    }
}
