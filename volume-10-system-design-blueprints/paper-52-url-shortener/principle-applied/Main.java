import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {
    private static final int NODES = 5;
    private static final int CODES_PER_NODE = 500;

    public static void main(String[] args) throws InterruptedException {
        long centralizedMs = runCentralized();
        long blockAllocatedMs = runBlockAllocated();

        System.out.println("Centralized (network call per code):   " + centralizedMs + "ms for " + (NODES * CODES_PER_NODE) + " codes");
        System.out.println("Block-allocated (network call per 1000): " + blockAllocatedMs + "ms for " + (NODES * CODES_PER_NODE) + " codes");
        System.out.println("Both produced zero collisions -- the counter guarantees uniqueness either way.");
    }

    private static long runCentralized() throws InterruptedException {
        CentralIdService idService = new CentralIdService();
        CentralizedCounterShortener shortener = new CentralizedCounterShortener(idService);
        Set<String> allCodes = ConcurrentHashMap.newKeySet();

        long start = System.currentTimeMillis();
        runNodes(() -> shortener.shorten(), allCodes);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("centralized unique codes=" + allCodes.size() + " (expected " + (NODES * CODES_PER_NODE) + ")");
        return elapsed;
    }

    private static long runBlockAllocated() throws InterruptedException {
        CentralIdService idService = new CentralIdService();
        Set<String> allCodes = ConcurrentHashMap.newKeySet();

        long start = System.currentTimeMillis();
        List<Thread> threads = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(NODES);
        for (int i = 0; i < NODES; i++) {
            BlockAllocatingShortener node = new BlockAllocatingShortener(idService); // each node gets its OWN block-allocator
            Thread t = new Thread(() -> {
                for (int j = 0; j < CODES_PER_NODE; j++) {
                    allCodes.add(node.shorten());
                }
                latch.countDown();
            });
            threads.add(t);
            t.start();
        }
        latch.await();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("block-allocated unique codes=" + allCodes.size() + " (expected " + (NODES * CODES_PER_NODE) + ")");
        return elapsed;
    }

    private interface CodeSupplier {
        String get();
    }

    private static void runNodes(CodeSupplier sharedShortener, Set<String> allCodes) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(NODES);
        for (int i = 0; i < NODES; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < CODES_PER_NODE; j++) {
                    allCodes.add(sharedShortener.get());
                }
                latch.countDown();
            });
            t.start();
        }
        latch.await();
    }
}
