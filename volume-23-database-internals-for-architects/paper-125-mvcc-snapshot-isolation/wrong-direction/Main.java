import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates that a single global lock held for a whole read
 * transaction does fix read skew -- the reader's total is always
 * conserved -- but at the cost of blocking every writer for as long as
 * the reader holds the lock, even though the writer touches accounts
 * the reader isn't even reading.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        LockedAccounts accounts = new LockedAccounts();
        accounts.open("A", 100);
        accounts.open("B", 100);

        int expectedTotal = 200;
        int readTransactionMillis = 200;

        CountDownLatch readerStarted = new CountDownLatch(1);
        final int[] observedA = new int[1];
        final int[] observedB = new int[1];
        final long[] writerElapsedMs = new long[1];

        Thread reader = new Thread(() -> {
            accounts.beginReadTransaction();
            readerStarted.countDown();
            observedA[0] = accounts.readWithinTransaction("A");
            try {
                // Simulates the reader doing real work (e.g. generating a
                // report) while still holding the transaction's lock.
                Thread.sleep(readTransactionMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            observedB[0] = accounts.readWithinTransaction("B");
            accounts.endReadTransaction();
        });

        Thread writer = new Thread(() -> {
            try {
                readerStarted.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long start = System.nanoTime();
            // This transfer touches A and B directly, but still has to
            // wait for the reader's lock to be released before it can run.
            accounts.write("A", accounts.read("A") - 50);
            accounts.write("B", accounts.read("B") + 50);
            writerElapsedMs[0] = (System.nanoTime() - start) / 1_000_000;
        });

        reader.start();
        writer.start();
        reader.join();
        writer.join();

        int observedTotal = observedA[0] + observedB[0];
        System.out.println("Reader observed A: " + observedA[0]);
        System.out.println("Reader observed B: " + observedB[0]);
        System.out.println("Reader's computed total: " + observedTotal);
        System.out.println("Expected total (conserved): " + expectedTotal);
        System.out.println("Read skew occurred: " + (observedTotal != expectedTotal));
        System.out.println("Reader held its transaction lock for: " + readTransactionMillis + "ms");
        System.out.println("Writer was blocked for approximately: " + writerElapsedMs[0] + "ms");
        System.out.println("Writer was blocked for the read transaction's duration: "
                + (writerElapsedMs[0] >= readTransactionMillis));
    }
}
