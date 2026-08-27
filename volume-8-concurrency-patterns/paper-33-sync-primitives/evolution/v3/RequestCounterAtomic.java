import java.util.concurrent.atomic.AtomicInteger;

class RequestCounterAtomic {
    private final AtomicInteger inFlight = new AtomicInteger(0);

    void requestStarted() {
        inFlight.incrementAndGet();
    }

    void requestFinished() {
        inFlight.decrementAndGet();
    }

    int getInFlight() {
        return inFlight.get();
    }
}
