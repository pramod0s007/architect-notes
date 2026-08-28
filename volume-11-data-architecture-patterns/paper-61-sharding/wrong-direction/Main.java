import java.util.concurrent.CountDownLatch;

// The overcorrection: the sharding infrastructure is identical to the fix --
// 16 shards, same hash-based routing. The mistake is WHAT gets hashed.
// Sharding by region for data-locality reasons sounds reasonable, right up
// until almost every active account is in the same region.
public class Main {
    private static final int SHARD_COUNT = 16;

    public static void main(String[] args) throws InterruptedException {
        for (int accountCount : new int[]{10, 100, 500}) {
            ShardedEventStore store = new ShardedEventStore(SHARD_COUNT);

            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch doneGate = new CountDownLatch(accountCount);
            for (int i = 0; i < accountCount; i++) {
                String region = regionFor(i); // the shard key -- NOT the account ID
                Thread t = new Thread(() -> {
                    await(startGate);
                    store.append(region, 100);
                    doneGate.countDown();
                });
                t.start();
            }

            long start = System.nanoTime();
            startGate.countDown();
            doneGate.await();
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("%d accounts appending concurrently, %d shards, sharded by region: total time %dms%n", accountCount, SHARD_COUNT, elapsedMs);
        }
    }

    // 98% of accounts are in the same region -- a plausible skew, since a
    // dominant region is common on real platforms even if this exact ratio isn't.
    private static String regionFor(int accountIndex) {
        return (accountIndex % 50 == 0) ? "us-west" : "us-east";
    }

    private static void await(CountDownLatch gate) {
        try {
            gate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
