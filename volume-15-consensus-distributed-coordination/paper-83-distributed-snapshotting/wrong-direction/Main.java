public class Main {
    // The overcorrection: instead of a marker traveling on the actual data
    // channel, coordinate out-of-band with a GUESSED safety delay -- "wait
    // long enough for any in-flight message to have arrived, then snapshot."
    static final int ASSUMED_SAFE_DELAY_TICKS = 5;

    public static void main(String[] args) {
        Node a = new Node("A", 100);
        Node b = new Node("B", 0);

        System.out.println("A transfers 50 to B, exactly as before.");
        a.balance -= 50;
        int messageSentAtTick = 0;
        int messageActuallyArrivesAtTick = 8; // real network variance -- slower than assumed
        System.out.println("A's balance is now " + a.balance + ". The transfer is in flight, arriving at tick "
            + messageActuallyArrivesAtTick + " this time.");

        int snapshotA = a.balance;
        int bSnapshotTick = messageSentAtTick + ASSUMED_SAFE_DELAY_TICKS;
        System.out.println("A's local snapshot taken: " + snapshotA + ". A tells B out-of-band: "
            + "\"wait " + ASSUMED_SAFE_DELAY_TICKS + " ticks, then snapshot yourself\" -- a guessed safety margin, "
            + "not a marker on the actual channel the transfer is traveling on.");

        System.out.println("B's timer fires at tick " + bSnapshotTick + ".");
        boolean messageArrivedInTime = messageActuallyArrivesAtTick <= bSnapshotTick;
        int snapshotB = messageArrivedInTime ? b.balance + 50 : b.balance;
        System.out.println("Has the transfer actually arrived by then? " + messageArrivedInTime);
        System.out.println("B's local snapshot taken: " + snapshotB);

        // The real system still catches up correctly a moment later.
        b.balance += 50;

        int recordedTotal = snapshotA + snapshotB;
        System.out.println();
        System.out.println("Recorded global snapshot total: " + snapshotA + " + " + snapshotB + " = " + recordedTotal);
        System.out.println("Actual total money in the system: " + (a.balance + b.balance));

        System.out.println(recordedTotal != (a.balance + b.balance)
            ? "STILL FAILS, UNDER REAL VARIANCE: the assumed " + ASSUMED_SAFE_DELAY_TICKS + "-tick safety "
              + "margin looked generous, but the actual message took " + messageActuallyArrivesAtTick
              + " ticks this time -- slower than the guess. B's timeout fired before the transfer arrived, "
              + "reproducing the exact same missing-$50 snapshot as having no coordination at all. A fixed "
              + "delay is a GUESS about message timing; a marker on the real channel requires no guess, "
              + "because it's ordered by the channel's own actual delivery, not by elapsed time."
            : "Unexpected -- investigate.");
    }
}
