import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Simulates a non-blocking I/O call: "completes" after ioWaitMs WITHOUT ever
// blocking a worker thread. A single scheduler thread manages the timer for
// every in-flight call — this is the client-side analogue of Paper 32's reactor.
class AsyncIoSimulator {
    private final ScheduledExecutorService scheduler;

    AsyncIoSimulator(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    CompletableFuture<Void> call(long ioWaitMs) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        scheduler.schedule(() -> future.complete(null), ioWaitMs, TimeUnit.MILLISECONDS);
        return future;
    }
}
