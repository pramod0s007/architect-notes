import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class SimpleWorker implements Runnable {
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    final AtomicInteger iterations = new AtomicInteger(0);

    public void run() {
        while (!stopRequested.get()) {
            iterations.incrementAndGet();
        }
    }

    void requestStop() {
        stopRequested.set(true);
    }
}
