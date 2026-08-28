import java.util.concurrent.atomic.AtomicLong;

// Stands in for a single shared counter row in a central database. Every
// call pays a simulated network round trip -- exactly what a real remote
// counter service costs, no matter how fast the increment itself is.
class CentralIdService {
    private final AtomicLong counter = new AtomicLong(0);

    long nextId() {
        simulateNetworkRoundTrip();
        return counter.incrementAndGet();
    }

    long allocateBlock(int blockSize) {
        simulateNetworkRoundTrip();
        return counter.getAndAdd(blockSize);
    }

    private void simulateNetworkRoundTrip() {
        try {
            Thread.sleep(2); // a realistic same-datacenter round trip, in milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
