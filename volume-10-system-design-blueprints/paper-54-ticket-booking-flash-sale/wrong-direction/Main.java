import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

// The overcorrection: "optimistic beat pessimistic under moderate load, so
// optimistic must always be the better choice." This scenario is the same
// flash sale, but at the moment it matters most -- the last few seats, with
// far more buyers than seats -- exactly the shape of a real tatkal window.
public class Main {
    private static final int SEATS = 20;
    private static final int REQUESTS = 5000;

    public static void main(String[] args) throws InterruptedException {
        runPessimistic();
        runOptimistic();
    }

    private static void runPessimistic() throws InterruptedException {
        PessimisticTicketInventory inventory = new PessimisticTicketInventory(SEATS);
        AtomicInteger successes = new AtomicInteger(0);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(REQUESTS);
        long start = System.nanoTime();
        for (int i = 0; i < REQUESTS; i++) {
            Thread t = new Thread(() -> {
                await(startGate);
                if (inventory.bookSeat()) successes.incrementAndGet();
                doneGate.countDown();
            });
            t.start();
        }
        startGate.countDown();
        doneGate.await();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Pessimistic, " + SEATS + " seats vs " + REQUESTS + " buyers: successes="
                + successes.get() + ", elapsed=" + elapsedMs + "ms");
    }

    private static void runOptimistic() throws InterruptedException {
        OptimisticTicketInventory inventory = new OptimisticTicketInventory(SEATS);
        AtomicInteger successes = new AtomicInteger(0);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(REQUESTS);
        long start = System.nanoTime();
        for (int i = 0; i < REQUESTS; i++) {
            Thread t = new Thread(() -> {
                await(startGate);
                if (inventory.bookSeat()) successes.incrementAndGet();
                doneGate.countDown();
            });
            t.start();
        }
        startGate.countDown();
        doneGate.await();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Optimistic, " + SEATS + " seats vs " + REQUESTS + " buyers: successes="
                + successes.get() + ", elapsed=" + elapsedMs + "ms, wasted retries=" + inventory.retryCount());
    }

    private static void await(CountDownLatch gate) {
        try {
            gate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
