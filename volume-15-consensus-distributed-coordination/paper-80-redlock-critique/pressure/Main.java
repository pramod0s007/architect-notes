public class Main {
    static final double TTL_MS = 100;

    public static void main(String[] args) {
        // 5 independent lock servers -- S1's clock runs 40% fast. This is
        // exactly the scenario Redlock is built on: acquire a majority (3 of 5)
        // to hold the lock.
        LockServer s1 = new LockServer("S1", 1.4);
        LockServer s2 = new LockServer("S2", 1.0);
        LockServer s3 = new LockServer("S3", 1.0);
        LockServer s4 = new LockServer("S4", 1.0);
        LockServer s5 = new LockServer("S5", 1.0);

        System.out.println("Client A acquires the lock at t=0 from S1, S2, S3 (majority = 3 of 5).");
        boolean a1 = s1.tryAcquire(0, TTL_MS);
        boolean a2 = s2.tryAcquire(0, TTL_MS);
        boolean a3 = s3.tryAcquire(0, TTL_MS);
        System.out.println("Client A holds the lock (by its own clock) until t=" + TTL_MS + ".");

        double clientBAttemptTime = 80;
        System.out.println();
        System.out.println("Client B attempts to acquire the SAME lock at t=" + clientBAttemptTime
            + " -- well before Client A's t=100 expiry, by any client's wall clock.");

        boolean b1 = s1.tryAcquire(clientBAttemptTime, TTL_MS); // S1's fast clock already expired A's lock
        boolean b4 = s4.tryAcquire(clientBAttemptTime, TTL_MS); // never asked by A, free
        boolean b5 = s5.tryAcquire(clientBAttemptTime, TTL_MS); // never asked by A, free
        boolean b2 = s2.tryAcquire(clientBAttemptTime, TTL_MS); // still holds A's lock
        boolean b3 = s3.tryAcquire(clientBAttemptTime, TTL_MS); // still holds A's lock

        int bGrants = (b1 ? 1 : 0) + (b2 ? 1 : 0) + (b3 ? 1 : 0) + (b4 ? 1 : 0) + (b5 ? 1 : 0);
        System.out.println("S1 grants to B: " + b1 + " (its clock already considers A's lock expired)");
        System.out.println("S2 grants to B: " + b2 + ", S3 grants to B: " + b3 + " (still hold A's lock)");
        System.out.println("S4 grants to B: " + b4 + ", S5 grants to B: " + b5 + " (never asked by A)");
        System.out.println("Client B's total grants: " + bGrants + " (majority = 3)");

        System.out.println();
        System.out.println("Both Client A (via S1,S2,S3 from t=0) and Client B (via S1,S4,S5 from t=" + clientBAttemptTime
            + ") now believe they hold the majority lock.");
        System.out.println(bGrants >= 3
            ? "CONFIRMED FAILURE: two clients simultaneously believe they hold the same distributed "
              + "lock. A single server's clock running fast let it expire and re-grant a lock that "
              + "every other client's own wall clock still considers valid."
            : "Unexpected -- investigate.");
    }
}
