/**
 * The actual fix: the client remembers the version number returned by
 * its own write, and a read is only served from the replica once the
 * replica's applied version has caught up to at least that version.
 * If it hasn't, the read falls back to the primary instead of risking
 * a stale or wrong value. This holds regardless of how long the
 * actual replication lag turns out to be -- no fixed wait is needed --
 * and once the replica does catch up, reads are served from it again
 * without any further fallback.
 */
public class Main {
    public static void main(String[] args) {
        Primary primary = new Primary();
        Replica replica = new Replica();
        ReplicationLog log = new ReplicationLog();

        int tick = 0;

        // Case 1: a write with only 1 tick of actual replication lag.
        int v1 = primary.write("profile:42", "Jane Doe (v1)");
        log.enqueue(v1, "profile:42", "Jane Doe (v1)", 1, tick);
        String read1 = readYourWrites(primary, replica, log, "profile:42", v1, tick);
        boolean correct1 = "Jane Doe (v1)".equals(read1);

        tick += 1; // time passes; give the replica a chance to catch up

        // Case 2: a write with 5 ticks of actual replication lag,
        // read back immediately (worse lag than Case 1, and worse
        // than the wrong-direction scenario's fixed 2-tick wait).
        int v2 = primary.write("profile:42", "Jane Doe (v2)");
        log.enqueue(v2, "profile:42", "Jane Doe (v2)", 5, tick);
        String read2 = readYourWrites(primary, replica, log, "profile:42", v2, tick);
        boolean correct2 = "Jane Doe (v2)".equals(read2);

        // Advance far enough for the second write to actually
        // replicate, then confirm a later read is now served from the
        // replica itself, not the primary fallback.
        tick += 5;
        String read3 = readYourWrites(primary, replica, log, "profile:42", v2, tick);
        boolean correct3 = "Jane Doe (v2)".equals(read3);
        boolean servedFromReplica = replica.getAppliedVersion() >= v2;

        System.out.println("Case 1 (1-tick actual lag, read immediately): value = " + read1 + ", correct = " + correct1);
        System.out.println("Case 2 (5-tick actual lag, read immediately): value = " + read2 + ", correct = " + correct2);
        System.out.println("Case 3 (read after replica caught up): value = " + read3 + ", correct = " + correct3 + ", served from replica = " + servedFromReplica);
    }

    // Reads its own writes correctly regardless of actual replication
    // lag: serves from the replica only once the replica has applied
    // at least the version this client itself just wrote; otherwise
    // falls back to the primary rather than risking a stale read.
    private static String readYourWrites(Primary primary, Replica replica, ReplicationLog log, String key, int minVersion, int currentTick) {
        applyReady(log, replica, currentTick);
        if (replica.getAppliedVersion() >= minVersion) {
            return replica.read(key);
        }
        return primary.read(key);
    }

    private static void applyReady(ReplicationLog log, Replica replica, int currentTick) {
        for (ReplicationLog.Entry e : log.drainReady(currentTick)) {
            replica.apply(e);
        }
    }
}
