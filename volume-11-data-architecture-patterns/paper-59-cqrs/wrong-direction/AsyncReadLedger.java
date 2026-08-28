import java.util.concurrent.atomic.AtomicLong;

// The overcorrection: decoupling reads from writes gets taken further, into
// replicating updates to the read side on a delay -- a realistic stand-in
// for a real replication pipeline (CDC, a message queue, a batch job).
class AsyncReadLedger {
    private final AtomicLong balanceCents = new AtomicLong(0);

    void applyWithLag(long amountCents, long lagMs) {
        new Thread(() -> {
            try {
                Thread.sleep(lagMs); // the update is genuinely in flight, not lost
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            balanceCents.addAndGet(amountCents);
        }).start();
    }

    long balance() {
        return balanceCents.get();
    }
}
