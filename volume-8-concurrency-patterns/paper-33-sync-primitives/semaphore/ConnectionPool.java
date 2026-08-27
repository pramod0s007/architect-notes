import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

// A lock guards exclusive access (0 or 1 holder). A Semaphore guards BOUNDED
// concurrent access — up to N holders at once, not just one.
class ConnectionPool {
    private final Semaphore permits;
    private final AtomicInteger concurrentUsers = new AtomicInteger(0);
    private final AtomicInteger maxObservedConcurrency = new AtomicInteger(0);

    ConnectionPool(int maxConcurrent) {
        this.permits = new Semaphore(maxConcurrent);
    }

    void useConnection(Runnable work) throws InterruptedException {
        permits.acquire();
        try {
            int current = concurrentUsers.incrementAndGet();
            maxObservedConcurrency.updateAndGet(max -> Math.max(max, current));
            work.run();
        } finally {
            concurrentUsers.decrementAndGet();
            permits.release();
        }
    }

    int getMaxObservedConcurrency() {
        return maxObservedConcurrency.get();
    }
}
