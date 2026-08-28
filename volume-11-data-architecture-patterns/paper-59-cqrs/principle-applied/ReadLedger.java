import java.util.concurrent.ConcurrentLinkedQueue;

// Owns the read path. Receiving an update (apply) is a lock-free queue
// append -- it never blocks on, or gets blocked by, a slow statement()
// call iterating the same queue.
class ReadLedger {
    private final ConcurrentLinkedQueue<Transaction> transactions = new ConcurrentLinkedQueue<>();

    void apply(long amountCents) {
        transactions.add(new Transaction(amountCents));
    }

    long statement() {
        long total = 0;
        for (Transaction t : transactions) {
            total += t.amountCents;
            simulateRenderingCost();
        }
        return total;
    }

    private void simulateRenderingCost() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
