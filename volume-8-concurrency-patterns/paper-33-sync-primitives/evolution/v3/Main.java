import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final int THREADS = 20;
    static final int INCREMENTS_PER_THREAD = 2500;
    static final int EXPECTED = THREADS * INCREMENTS_PER_THREAD;

    public static void main(String[] args) throws InterruptedException {
        RequestCounterSynchronized sync = new RequestCounterSynchronized();
        runStartAndFinish(sync::requestStarted, sync::requestFinished);
        System.out.println("synchronized:   expected 0, got " + sync.getInFlight());

        RequestCounterAtomic atomic = new RequestCounterAtomic();
        runStartAndFinish(atomic::requestStarted, atomic::requestFinished);
        System.out.println("AtomicInteger:  expected 0, got " + atomic.getInFlight());

        demonstrateBrokenInvariant();
    }

    private static void runStartAndFinish(Runnable start, Runnable finish) throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    start.run();
                    finish.run();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }

    // Demonstrates that per-field locking doesn't protect the cross-field invariant:
    // a checker thread hunts for a moment where inFlight + completed != totalStarted.
    private static void demonstrateBrokenInvariant() throws InterruptedException {
        BrokenTransferCounter counter = new BrokenTransferCounter();
        int totalStarted = 200_000;
        for (int i = 0; i < totalStarted; i++) counter.requestStarted();

        AtomicBoolean stop = new AtomicBoolean(false);
        AtomicInteger violationsSeen = new AtomicInteger(0);

        Thread worker = new Thread(() -> {
            for (int i = 0; i < totalStarted; i++) counter.requestFinished();
            stop.set(true);
        });

        Thread checker = new Thread(() -> {
            while (!stop.get()) {
                int inFlight = counter.getInFlight();
                int completed = counter.getCompleted();
                if (inFlight + completed != totalStarted) {
                    violationsSeen.incrementAndGet();
                }
            }
        });

        worker.start();
        checker.start();
        worker.join();
        checker.join();

        System.out.println("Broken invariant: " + violationsSeen.get()
                + " moments observed where inFlight + completed != totalStarted");
    }
}
