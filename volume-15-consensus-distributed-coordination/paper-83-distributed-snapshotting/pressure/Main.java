public class Main {
    public static void main(String[] args) {
        Node a = new Node("A", 100);
        Node b = new Node("B", 0);

        System.out.println("A transfers 50 to B. A applies the deduction immediately.");
        a.balance -= 50; // A: 100 -> 50
        System.out.println("A's balance is now " + a.balance + ". The transfer message is in flight to B.");

        // Each node's snapshot is taken independently, at whatever moment is
        // convenient locally -- no coordination between them at all.
        int snapshotA = a.balance;
        System.out.println("A's local snapshot taken: " + snapshotA);

        int snapshotB = b.balance; // taken BEFORE the in-flight message has arrived
        System.out.println("B's local snapshot taken (before the transfer message arrives): " + snapshotB);

        // The real system keeps running normally -- the message arrives and B
        // applies it a moment later. This is fine for the LIVE system. It's the
        // already-recorded SNAPSHOT that's now wrong.
        b.balance += 50; // B: 0 -> 50, applied after both snapshots were taken
        System.out.println("(Afterward, the real system continues correctly: B's live balance is now " + b.balance + ".)");

        int recordedTotal = snapshotA + snapshotB;
        System.out.println();
        System.out.println("Recorded global snapshot total: " + snapshotA + " + " + snapshotB + " = " + recordedTotal);
        System.out.println("Actual total money in the system (verifiable from the live system): " + (a.balance + b.balance));

        System.out.println(recordedTotal != (a.balance + b.balance)
            ? "CONFIRMED FAILURE: the recorded snapshot is missing $" + ((a.balance + b.balance) - recordedTotal)
              + ". A's snapshot shows the money already deducted; B's snapshot was taken before it arrived. "
              + "The $50 is real, it's just in transit -- and this snapshot has no way to represent \"in "
              + "transit,\" so it silently vanishes from the recorded state."
            : "Unexpected -- investigate.");
    }
}
