import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        for (int accountCount : new int[]{10, 100, 500}) {
            SingleNodeEventStore store = new SingleNodeEventStore();

            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch doneGate = new CountDownLatch(accountCount);
            for (int i = 0; i < accountCount; i++) {
                String accountId = "account-" + i;
                Thread t = new Thread(() -> {
                    await(startGate);
                    store.append(accountId, 100); // every account funnels through the SAME lock
                    doneGate.countDown();
                });
                t.start();
            }

            long start = System.nanoTime();
            startGate.countDown();
            doneGate.await();
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("%d accounts appending concurrently, one node: total time %dms%n", accountCount, elapsedMs);
        }
    }

    private static void await(CountDownLatch gate) {
        try {
            gate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
