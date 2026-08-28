import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        long[] breakerOnly = runBreakerOnly();
        long[] bulkheadOnly = runBulkheadOnly();
        long[] both = runBoth();

        System.out.println("v3: already-paid order latency (max / avg), dead payment backend --");
        System.out.println("    circuit breaker only, shared pool : max=" + breakerOnly[0] + "ms, avg=" + breakerOnly[1] + "ms");
        System.out.println("    bulkhead only, no breaker         : max=" + bulkheadOnly[0] + "ms, avg=" + bulkheadOnly[1] + "ms");
        System.out.println("    both together                     : max=" + both[0] + "ms, avg=" + both[1] + "ms");
        System.out.println("    (v2's baseline, no fix at all, was max=605ms avg=590ms.)");
    }

    // Scenario A: one shared pool, PaymentRouter wrapped with a circuit breaker.
    // The breaker fails fast once tripped -- but the first wave of concurrent
    // calls that trips it still pays the full retry cost before it opens.
    private static long[] runBreakerOnly() throws Exception {
        CircuitBreaker breaker = new CircuitBreaker(2, 200);
        PaymentRouterWithBreaker paymentRouter = new PaymentRouterWithBreaker(breaker);
        InventoryService inventory = new InventoryService();
        ShippingService shipping = new ShippingService();

        ExecutorService sharedPool = Executors.newFixedThreadPool(4);
        Future<?>[] paymentFutures = new Future<?>[6];
        for (int i = 0; i < 6; i++) {
            Order order = new Order("PAY-" + i);
            paymentFutures[i] = sharedPool.submit(() -> {
                paymentRouter.charge(order);
                return null;
            });
        }

        long[] result = measureFastOrders(sharedPool, inventory, shipping);
        for (Future<?> f : paymentFutures) f.get();
        sharedPool.shutdown();
        sharedPool.awaitTermination(5, TimeUnit.SECONDS);
        return result;
    }

    // Scenario B: no breaker at all -- naive retry-forever PaymentRouter -- but
    // payment calls run on their OWN small pool, structurally separate from the
    // fulfillment pool that already-paid orders use.
    private static long[] runBulkheadOnly() throws Exception {
        PaymentRouter paymentRouter = new PaymentRouter();
        InventoryService inventory = new InventoryService();
        ShippingService shipping = new ShippingService();

        Bulkhead paymentBulkhead = new Bulkhead(2);
        ExecutorService fulfillmentPool = Executors.newFixedThreadPool(4);

        Future<?>[] paymentFutures = new Future<?>[6];
        for (int i = 0; i < 6; i++) {
            Order order = new Order("PAY-" + i);
            paymentFutures[i] = paymentBulkhead.submit(() -> paymentRouter.charge(order));
        }

        long[] result = measureFastOrders(fulfillmentPool, inventory, shipping);
        for (Future<?> f : paymentFutures) f.get();
        paymentBulkhead.shutdown();
        fulfillmentPool.shutdown();
        fulfillmentPool.awaitTermination(5, TimeUnit.SECONDS);
        return result;
    }

    // Scenario C: breaker (fails fast once tripped) AND bulkhead (structurally
    // isolated pool) together -- the combination Hystrix popularized per-dependency.
    private static long[] runBoth() throws Exception {
        CircuitBreaker breaker = new CircuitBreaker(2, 200);
        PaymentRouterWithBreaker paymentRouter = new PaymentRouterWithBreaker(breaker);
        InventoryService inventory = new InventoryService();
        ShippingService shipping = new ShippingService();

        Bulkhead paymentBulkhead = new Bulkhead(2);
        ExecutorService fulfillmentPool = Executors.newFixedThreadPool(4);

        Future<?>[] paymentFutures = new Future<?>[6];
        for (int i = 0; i < 6; i++) {
            Order order = new Order("PAY-" + i);
            paymentFutures[i] = paymentBulkhead.submit(() -> paymentRouter.charge(order));
        }

        long[] result = measureFastOrders(fulfillmentPool, inventory, shipping);
        for (Future<?> f : paymentFutures) f.get();
        paymentBulkhead.shutdown();
        fulfillmentPool.shutdown();
        fulfillmentPool.awaitTermination(5, TimeUnit.SECONDS);
        return result;
    }

    private static long[] measureFastOrders(ExecutorService pool, InventoryService inventory, ShippingService shipping) throws Exception {
        long[] latencyMs = new long[6];
        Future<?>[] fastFutures = new Future<?>[6];
        long submitTime = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            Order order = new Order("FAST-" + (i + 1));
            fastFutures[i] = pool.submit(() -> {
                inventory.reserve(order);
                shipping.schedule(order);
                latencyMs[idx] = System.currentTimeMillis() - submitTime;
                return null;
            });
        }
        for (Future<?> f : fastFutures) f.get();

        long max = 0, total = 0;
        for (long ms : latencyMs) {
            max = Math.max(max, ms);
            total += ms;
        }
        return new long[] { max, total / 6 };
    }
}
