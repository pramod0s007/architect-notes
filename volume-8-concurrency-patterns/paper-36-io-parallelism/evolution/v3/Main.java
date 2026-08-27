import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    public static void main(String[] args) throws Exception {
        int callCount = 400;
        long ioWaitMs = 50;

        int sizedPoolSize = SizedPool.sizeFor(50.0, 64); // matches the "Principle Applied" example
        long elapsedSized = SizedPool.runBatch(callCount, ioWaitMs, sizedPoolSize);
        System.out.println("Sized pool (" + sizedPoolSize + " threads): " + elapsedSized + "ms");

        runAsyncDemo(callCount, ioWaitMs);
    }

    private static void runAsyncDemo(int callCount, long ioWaitMs) throws Exception {
        int schedulerThreads = 4; // NOT one thread per in-flight call
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(schedulerThreads);
        AsyncIoSimulator io = new AsyncIoSimulator(scheduler);

        long start = System.nanoTime();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < callCount; i++) {
            futures.add(io.call(ioWaitMs));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        scheduler.shutdown();
        System.out.println("Async (" + schedulerThreads + " scheduler threads), "
                + callCount + " concurrent in-flight calls: " + elapsed + "ms");
        System.out.println("v3: " + callCount + " concurrent waits managed by " + schedulerThreads
                + " real threads -- no thread was ever blocked waiting.");
    }
}
