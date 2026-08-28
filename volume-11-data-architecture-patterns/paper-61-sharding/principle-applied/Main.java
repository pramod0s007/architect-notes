import java.util.concurrent.CountDownLatch;

public class Main {
    private static final int SHARD_COUNT = 16;

    public static void main(String[] args) throws InterruptedException {
        for (int accountCount : new int[]{10, 100, 500}) {
            ShardedEventStore store = new ShardedEventStore(SHARD_COUNT);

            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch doneGate = new CountDownLatch(accountCount);
            for (int i = 0; i < accountCount; i++) {
                String accountId = "account-" + i;
                Thread t = new Thread(() -> {
                    await(startGate);
                    store.append(accountId, 100);
                    doneGate.countDown();
                });
                t.start();
            }

            long start = System.nanoTime();
            startGate.countDown();
            doneGate.await();
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("%d accounts appending concurrently, %d shards: total time %dms%n", accountCount, SHARD_COUNT, elapsedMs);
        }
    }

    private static void await(CountDownLatch gate) {
        try {
            gate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
