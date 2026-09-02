public class Main {
    static final long LEASE_DURATION_MS = 100;

    public static void main(String[] args) {
        // The overcorrection: instead of majority voting, use a simple lease --
        // whoever holds the lease is leader, until it expires. This sounds like
        // it prevents two leaders. It doesn't protect against a leader that's
        // still ALIVE but too slow (a long GC pause, a network delay) to notice
        // its own lease already expired.
        SharedStore store = new SharedStore();

        System.out.println("N1 acquires a " + LEASE_DURATION_MS + "ms lease at t=0 and becomes leader.");
        long n1LeaseAcquiredAt = 0;
        store.write("N1", 100);

        System.out.println("N1 pauses (GC, slow disk, network delay) at t=10 and doesn't renew in time.");
        long n1LeaseExpiresAt = n1LeaseAcquiredAt + LEASE_DURATION_MS;
        System.out.println("N1's lease expires at t=" + n1LeaseExpiresAt + ".");

        long n2LeaseAcquiredAt = n1LeaseExpiresAt + 1;
        System.out.println("N2 correctly acquires a new lease at t=" + n2LeaseAcquiredAt + " and becomes the new leader.");
        store.write("N2", 200);
        System.out.println("N2 writes value=200 as the new, current leader.");

        long n1ResumesAt = 150;
        System.out.println();
        System.out.println("N1 finally resumes at t=" + n1ResumesAt + " -- it never checked whether its "
            + "lease was still valid, because as far as its own execution is concerned, no time passed "
            + "at all while it was paused.");
        store.write("N1", 999); // N1's stale, in-flight write -- issued as if it were still leader.
        System.out.println("N1 sends its already-in-flight write (value=999), believing it's still leader.");

        System.out.println();
        System.out.println("Final stored value: " + store.currentValue() + ", last writer: " + store.lastWriter());
        System.out.println(store.lastWriter().equals("N1")
            ? "STILL FAILS: N2 is the actual current, valid leader -- but the store has no way to reject "
              + "a write from a node whose leadership already ended. N1's stale write silently overwrote "
              + "N2's, and nothing anywhere detected or prevented it. A lease alone stops two nodes from "
              + "BELIEVING they're leader at the same instant -- it does nothing to stop a former "
              + "leader's already-issued, in-flight write from landing after leadership has moved on."
            : "Unexpected -- investigate.");
    }
}
