/**
 * A common but fragile attempted fix: after writing, wait a fixed
 * number of ticks before reading from the replica, hoping that's
 * enough time for replication to catch up. This works when the actual
 * replication lag happens to be less than the fixed wait -- and fails,
 * reproducing the exact same read-your-writes violation, whenever the
 * real lag is longer than whatever constant was chosen.
 */
public class Main {
    private static final int FIXED_WAIT_TICKS = 2;

    public static void main(String[] args) {
        Primary primary = new Primary();
        Replica replica = new Replica();
        ReplicationLog log = new ReplicationLog();

        int tick = 0;

        // Case 1: this write happens to replicate in 1 tick -- the
        // fixed 2-tick wait happens to be enough.
        int v1 = primary.write("profile:42", "Jane Doe (v1)");
        log.enqueue(v1, "profile:42", "Jane Doe (v1)", 1, tick);
        tick += FIXED_WAIT_TICKS;
        applyReady(log, replica, tick);
        String read1 = replica.read("profile:42");
        boolean correct1 = "Jane Doe (v1)".equals(read1);

        // Case 2: this write takes 5 ticks to replicate (e.g. the
        // replica is briefly under load) -- the same fixed 2-tick
        // wait is not enough, and read-your-writes is violated again,
        // despite the "fix" being in place.
        int v2 = primary.write("profile:42", "Jane Doe (v2)");
        log.enqueue(v2, "profile:42", "Jane Doe (v2)", 5, tick);
        tick += FIXED_WAIT_TICKS;
        applyReady(log, replica, tick);
        String read2 = replica.read("profile:42");
        boolean correct2 = "Jane Doe (v2)".equals(read2);

        System.out.println("Case 1 (1-tick actual lag, waited " + FIXED_WAIT_TICKS + " ticks): replica value = " + read1 + ", correct = " + correct1);
        System.out.println("Case 2 (5-tick actual lag, waited " + FIXED_WAIT_TICKS + " ticks): replica value = " + read2 + ", correct = " + correct2);
    }

    private static void applyReady(ReplicationLog log, Replica replica, int currentTick) {
        for (ReplicationLog.Entry e : log.drainReady(currentTick)) {
            replica.apply(e);
        }
    }
}
