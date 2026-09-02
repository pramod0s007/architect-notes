public class Main {
    // The overcorrection: shrink the TTL, on the theory that a shorter window
    // gives clock drift less time to matter. Drift is proportional to elapsed
    // time, not a fixed amount -- shrinking the window shrinks the ABSOLUTE
    // drift by the same proportion, and the bug reappears at a smaller scale,
    // not a zero scale.
    static final double TTL_MS = 20;

    public static void main(String[] args) {
        LockServer s1 = new LockServer("S1", 1.4);
        LockServer s2 = new LockServer("S2", 1.0);
        LockServer s3 = new LockServer("S3", 1.0);
        LockServer s4 = new LockServer("S4", 1.0);
        LockServer s5 = new LockServer("S5", 1.0);

        System.out.println("Same clock drift, TTL shrunk from 100ms to " + TTL_MS + "ms.");
        System.out.println("Client A acquires the lock at t=0 from S1, S2, S3 (majority).");
        s1.tryAcquire(0, TTL_MS);
        s2.tryAcquire(0, TTL_MS);
        s3.tryAcquire(0, TTL_MS);
        System.out.println("Client A holds the lock (by its own clock) until t=" + TTL_MS + ".");

        double clientBAttemptTime = 16;
        System.out.println();
        System.out.println("Client B attempts at t=" + clientBAttemptTime + " -- still well before A's t="
            + TTL_MS + " expiry, by any client's wall clock.");

        boolean b1 = s1.tryAcquire(clientBAttemptTime, TTL_MS);
        boolean b4 = s4.tryAcquire(clientBAttemptTime, TTL_MS);
        boolean b5 = s5.tryAcquire(clientBAttemptTime, TTL_MS);
        int bGrants = (b1 ? 1 : 0) + (b4 ? 1 : 0) + (b5 ? 1 : 0);

        System.out.println("S1 grants to B: " + b1 + " (fast clock already expired A's grant, even at this smaller TTL)");
        System.out.println("Client B's total grants so far: " + bGrants + " (majority = 3)");

        System.out.println();
        System.out.println(bGrants >= 3
            ? "STILL FAILS, AT A SMALLER SCALE: shrinking the TTL by 5x shrank the vulnerable window by "
              + "the same 5x -- from a 20ms gap down to a 4ms gap -- but the double-grant still happens. "
              + "Drift is proportional to elapsed time, so scaling down the window scales down the "
              + "absolute drift by exactly the same factor, and the relative danger never changes. A "
              + "shorter TTL makes this bug rarer and harder to reproduce. It does not make it impossible."
            : "Unexpected -- investigate.");
    }
}
