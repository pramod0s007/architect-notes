import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates that MVCC fixes read skew without taking any lock that
 * blocks the writer: the reader's snapshot timestamp is captured once,
 * up front, and every read within the transaction is answered from
 * that fixed point in history -- even though a conflicting transfer
 * commits new versions of both accounts while the reader's transaction
 * is still in progress.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        VersionedStore accounts = new VersionedStore();
        accounts.write("A", 100);
        accounts.write("B", 100);

        int expectedTotal = 200;

        // The reader's snapshot is captured now, before the writer's
        // transfer commits anything -- everything the writer does after
        // this point is invisible to this transaction.
        long snapshotTs = accounts.currentTimestamp();

        CountDownLatch readerReadA = new CountDownLatch(1);
        CountDownLatch writerDone = new CountDownLatch(1);

        final int[] observedA = new int[1];
        final int[] observedB = new int[1];
        final long[] writerElapsedMs = new long[1];

        Thread reader = new Thread(() -> {
            observedA[0] = accounts.readAsOf("A", snapshotTs);
            readerReadA.countDown();
            try {
                writerDone.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Same snapshot timestamp as the first read -- the transfer
            // that just committed is simply not visible at this snapshot.
            observedB[0] = accounts.readAsOf("B", snapshotTs);
        });

        Thread writer = new Thread(() -> {
            try {
                readerReadA.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long start = System.nanoTime();
            long currentA = accounts.readAsOf("A", accounts.currentTimestamp());
            long currentB = accounts.readAsOf("B", accounts.currentTimestamp());
            accounts.write("A", (int) currentA - 50);
            accounts.write("B", (int) currentB + 50);
            writerElapsedMs[0] = (System.nanoTime() - start) / 1_000_000;
            writerDone.countDown();
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
        System.out.println("Writer completed its transfer in: " + writerElapsedMs[0] + "ms (no lock contention)");

        long afterTs = accounts.currentTimestamp();
        System.out.println("New snapshot after the transfer sees A=" + accounts.readAsOf("A", afterTs)
                + ", B=" + accounts.readAsOf("B", afterTs));
    }
}
