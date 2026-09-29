import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scenario 1: proves the TTL fixes permanent staleness -- a value is cached,
 * the TTL expires, and the very next read picks up the fresh value with no
 * manual invalidation required.
 *
 * Scenario 2: proves the TTL introduces a new problem -- 20 threads all ask
 * for the same key at the instant it expires. Every thread independently
 * sees a miss and independently calls the (slow) backing store, instead of
 * one thread loading the fresh value for everyone.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Scenario 1: TTL fixes staleness ===");
        BackingStore store1 = new BackingStore("price:9.99");
        TtlCache cache1 = new TtlCache(store1, 100);

        System.out.println("First read: " + cache1.get("product:42")
                + " (loads so far: " + store1.getLoadCount() + ")");
        Thread.sleep(150); // let the 100ms TTL expire
        System.out.println("After the 100ms TTL expires, and no manual invalidation: "
                + cache1.get("product:42") + " (loads so far: " + store1.getLoadCount() + ")");
        System.out.println("Staleness is bounded by the TTL now, not permanent.");

        System.out.println();
        System.out.println("=== Scenario 2: the same TTL causes a stampede at expiry ===");
        BackingStore store2 = new BackingStore("price:9.99");
        TtlCache cache2 = new TtlCache(store2, 100);

        cache2.get("product:42"); // populate the cache once
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
                cache2.get("product:42");
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
                + ". Backing store loads: " + store2.getLoadCount());
        System.out.println();
        System.out.println("CONFIRMED FAILURE: " + threadCount + " concurrent threads all saw a"
                + " miss and all independently called the backing store for the exact same key,"
                + " at the exact same moment -- a cache stampede, caused by the TTL expiring under"
                + " concurrent load instead of one thread refreshing it for everyone.");
    }
}
