import java.util.concurrent.atomic.AtomicInteger;

// Completely healthy. Tracks how many times it was actually invoked, so a
// test can prove whether calls reached it or were short-circuited before that.
class CountingInventoryBackend {
    private final AtomicInteger invocations = new AtomicInteger(0);

    boolean reserve(Order order) {
        invocations.incrementAndGet();
        return true;
    }

    int invocationCount() {
        return invocations.get();
    }
}
