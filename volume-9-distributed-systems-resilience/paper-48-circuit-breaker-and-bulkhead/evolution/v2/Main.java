import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        PaymentRouter paymentRouter = new PaymentRouter();
        InventoryService inventory = new InventoryService();
        ShippingService shipping = new ShippingService();

        // One shared pool handles every order's work -- new payments, and orders
        // that were already paid earlier and only need inventory + shipping now.
        ExecutorService sharedPool = Executors.newFixedThreadPool(4);

        List<Future<?>> futures = new ArrayList<>();

        // Six new orders: each one calls the dead PaymentRouter first.
        for (int i = 1; i <= 6; i++) {
            Order order = new Order("PAY-" + i);
            futures.add(sharedPool.submit(() -> {
                paymentRouter.charge(order); // ~540ms of retries against a dead backend
                return null;
            }));
        }

        // Six already-paid orders: healthy, fast -- inventory + shipping only.
        long[] fastOrderLatencyMs = new long[6];
        List<Future<?>> fastFutures = new ArrayList<>();
        long submitTime = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            Order order = new Order("FAST-" + (i + 1));
            fastFutures.add(sharedPool.submit(() -> {
                inventory.reserve(order);
                shipping.schedule(order);
                fastOrderLatencyMs[idx] = System.currentTimeMillis() - submitTime;
                return null;
            }));
        }

        for (Future<?> f : futures) f.get();
        for (Future<?> f : fastFutures) f.get();
        sharedPool.shutdown();
        sharedPool.awaitTermination(5, TimeUnit.SECONDS);

        long max = 0, total = 0;
        for (long ms : fastOrderLatencyMs) {
            max = Math.max(max, ms);
            total += ms;
        }
        System.out.println("v2: shared pool of 4 threads, dead payment backend, naive retry-forever.");
        System.out.println("    6 already-paid orders (healthy, ~10ms of real work) queued behind");
        System.out.println("    6 orders retrying a dead payment dependency on the SAME pool.");
        System.out.println("    Already-paid order latency: max=" + max + "ms, avg=" + (total / 6) + "ms");
        System.out.println("    (Baseline with no contention would be ~10-15ms.)");
    }
}
