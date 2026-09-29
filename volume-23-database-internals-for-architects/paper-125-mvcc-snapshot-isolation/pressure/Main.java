import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates read skew: a "report" transaction reads two account
 * balances that should always sum to a fixed total, but a concurrent
 * transfer between those same two accounts runs in between the two
 * reads. The reader ends up combining a pre-transfer value for one
 * account with a post-transfer value for the other -- a total that
 * never actually existed at any single point in time.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SharedAccounts accounts = new SharedAccounts();
        accounts.open("A", 100);
        accounts.open("B", 100);

        int expectedTotal = 200;

        // Latches force a deterministic interleaving: the reader reads A,
        // then the writer's transfer runs to completion, then the reader
        // reads B -- reproducing the race every run, not just sometimes.
        CountDownLatch readerReadA = new CountDownLatch(1);
        CountDownLatch writerDone = new CountDownLatch(1);

        final int[] observedA = new int[1];
        final int[] observedB = new int[1];

        Thread reader = new Thread(() -> {
            observedA[0] = accounts.read("A");
            readerReadA.countDown();
            try {
                writerDone.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            observedB[0] = accounts.read("B");
        });

        Thread writer = new Thread(() -> {
            try {
                readerReadA.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Transfer 50 from A to B -- total is conserved at every
            // committed instant, just not visible mid-transfer.
            accounts.write("A", accounts.read("A") - 50);
            accounts.write("B", accounts.read("B") + 50);
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
    }
}
