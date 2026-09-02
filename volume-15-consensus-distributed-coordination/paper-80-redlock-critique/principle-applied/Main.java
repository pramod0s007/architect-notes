public class Main {
    static final double TTL_MS = 100;

    public static void main(String[] args) {
        LockServer s1 = new LockServer("S1", 1.4);
        LockServer s2 = new LockServer("S2", 1.0);
        LockServer s3 = new LockServer("S3", 1.0);
        LockServer s4 = new LockServer("S4", 1.0);
        LockServer s5 = new LockServer("S5", 1.0);
        FencedResource resource = new FencedResource();

        // A fencing token is issued every time a client's majority acquisition
        // succeeds -- a monotonic counter, independent of any single server's
        // clock. It records ORDER of acquisition, not time.
        int fencingCounter = 0;

        System.out.println("Client A acquires the lock at t=0 from S1, S2, S3 (majority).");
        s1.tryAcquire(0, TTL_MS);
        s2.tryAcquire(0, TTL_MS);
        s3.tryAcquire(0, TTL_MS);
        int aToken = ++fencingCounter;
        System.out.println("Client A's fencing token: " + aToken);

        System.out.println();
        System.out.println("Client B acquires the SAME lock at t=80 (S1's fast clock already expired A's grant).");
        s1.tryAcquire(80, TTL_MS);
        s4.tryAcquire(80, TTL_MS);
        s5.tryAcquire(80, TTL_MS);
        int bToken = ++fencingCounter;
        System.out.println("Client B's fencing token: " + bToken + " (higher -- it acquired later)");

        System.out.println();
        System.out.println("Client B writes first, using its token:");
        boolean bWriteAccepted = resource.write(bToken, "B's update");
        System.out.println("  accepted=" + bWriteAccepted + ", resource value=" + resource.currentValue());

        System.out.println("Client A, still unaware its lock was ever double-granted, writes using its OLD token:");
        boolean aWriteAccepted = resource.write(aToken, "A's stale update");
        System.out.println("  accepted=" + aWriteAccepted + ", resource value=" + resource.currentValue());

        System.out.println();
        System.out.println(!aWriteAccepted && bWriteAccepted && resource.currentValue().equals("B's update")
            ? "FIXED: both clients still believed they held the lock -- the clock-drift bug at the "
              + "lock layer wasn't fixed, and wasn't the point. The resource itself rejected the "
              + "lower-numbered token, so A's stale write never took effect no matter what A believed."
            : "Unexpected -- investigate.");
    }
}
