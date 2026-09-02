public class Main {
    public static void main(String[] args) {
        Node a = new Node("A", 100);
        Node b = new Node("B", 0);
        Channel aToB = new Channel();

        System.out.println("A transfers 50 to B, exactly as before.");
        a.balance -= 50;
        aToB.send("TRANSFER:50");
        System.out.println("A's balance is now " + a.balance + ". The transfer message is queued on the FIFO channel to B.");

        // A initiates the snapshot: records its own state immediately, then
        // sends a MARKER on the same channel -- which, because the channel is
        // FIFO, is guaranteed to arrive at B AFTER the transfer message that
        // was already queued ahead of it.
        int snapshotA = a.balance;
        aToB.send("MARKER");
        System.out.println("A's local snapshot taken: " + snapshotA + ". A sends a MARKER right behind the transfer.");

        // B processes its channel in order. It hasn't taken its own snapshot
        // yet -- it just applies messages normally until it sees a marker.
        Integer snapshotB = null;
        int channelStateRecorded = 0;
        String message;
        while ((message = aToB.deliverNext()) != null) {
            if (message.equals("MARKER")) {
                if (snapshotB == null) {
                    // First marker on this channel: record B's state NOW.
                    snapshotB = b.balance;
                    System.out.println("B receives the MARKER. B's local snapshot taken: " + snapshotB
                        + " (the transfer was already applied before the marker arrived).");
                }
            } else if (message.startsWith("TRANSFER:")) {
                int amount = Integer.parseInt(message.substring("TRANSFER:".length()));
                b.balance += amount;
                System.out.println("B receives and applies the transfer before the marker: balance now " + b.balance);
            }
        }

        if (snapshotB == null) {
            throw new IllegalStateException("B never saw a marker -- this demo assumes A always sends one");
        }
        int recordedTotal = snapshotA + snapshotB + channelStateRecorded;
        System.out.println();
        System.out.println("Recorded global snapshot total: " + snapshotA + " + " + snapshotB
            + " + " + channelStateRecorded + " (in-channel) = " + recordedTotal);
        System.out.println("Actual total money in the system: " + (a.balance + b.balance));

        System.out.println(recordedTotal == (a.balance + b.balance)
            ? "FIXED: the recorded snapshot exactly matches reality. The marker travels behind the "
              + "transfer on the same FIFO channel, so B is guaranteed to process the transfer before "
              + "the marker ever tells it to take its own snapshot -- no guess about timing was needed, "
              + "just the channel's own delivery order."
            : "Unexpected -- investigate.");
    }
}
