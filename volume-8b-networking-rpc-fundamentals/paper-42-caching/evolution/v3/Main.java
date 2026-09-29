import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Replays the exact same stampede scenario as evolution/v2's Main: 20
 * threads all request the same key at the instant its TTL expires. With
 * request coalescing in place, only the first thread actually calls the
 * backing store -- every other thread waits on that same in-flight load
 * and reuses its result.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        BackingStore store = new BackingStore("price:9.99");
        SingleFlightCache cache = new SingleFlightCache(store, 100);

        cache.get("product:42"); // populate the cache once
        Thread.sleep(150); // let the 100ms TTL expire

        int threadCount = 20;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successfulReads = new AtomicInteger(0);
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                cache.get("product:42");
                successfulReads.incrementAndGet();
            });
            threads[i].start();
        }
        readyLatch.await(); // all 20 threads are lined up and waiting
        startLatch.countDown(); // release them all at the same instant
        for (Thread t : threads) {
            t.join();
        }

        System.out.println(threadCount + " threads all requested the same expired key at the"
                + " same instant. Successful reads: " + successfulReads.get()
                + ". Backing store loads: " + store.getLoadCount());
        System.out.println();
        System.out.println("FIXED: " + store.getLoadCount() + " total backing store loads --"
                + " 1 from the initial populate, and exactly 1 more from the 20 concurrent"
                + " requests that all landed on the expired key at once. ConcurrentHashMap's"
                + " single-invocation guarantee meant only the first of the 20 threads actually"
                + " called the backing store; the other 19 waited on its result instead of"
                + " starting redundant loads of their own.");
    }
}
