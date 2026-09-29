/**
 * Demonstrates that a fixed TTL bounds staleness but does not eliminate it:
 * within the TTL window, a node can still serve a value that's already wrong,
 * including on the very next read right after a write lands on another node.
 */
public class Main {
    public static void main(String[] args) {
        Backend backend = new Backend();
        CacheNode nodeA = new CacheNode("nodeA", backend);
        CacheNode nodeB = new CacheNode("nodeB", backend);

        backend.write("price:sku-42", "19.99");

        // Both nodes warm their caches at tick 0.
        int tick = 0;
        System.out.println("Warm read on nodeA at tick " + tick + ": " + nodeA.get("price:sku-42", tick));
        System.out.println("Warm read on nodeB at tick " + tick + ": " + nodeB.get("price:sku-42", tick));

        // A price update lands on nodeA at tick 3.
        int writeTick = 3;
        nodeA.write("price:sku-42", "24.99", writeTick);
        System.out.println("nodeA wrote new price 24.99 at tick " + writeTick);

        String immediateA = nodeA.get("price:sku-42", writeTick);
        System.out.println("Read on nodeA immediately after write (tick " + writeTick + "): " + immediateA);

        // The same caller re-reads through nodeB in the very same tick,
        // expecting to see its own write -- but TTL has no idea a write
        // just happened; nodeB's entry is only 3 ticks old, well inside its
        // 5-tick TTL, so it serves the stale value regardless of TTL length.
        String immediateB = nodeB.get("price:sku-42", writeTick);
        System.out.println("Read-your-own-write attempt on nodeB (tick " + writeTick + "): " + immediateB
                + (immediateB.equals("19.99") ? " -- STALE, fails read-your-own-write" : " -- fresh"));

        // Simulate nodeB serving reads across ticks 3 through 8 and count
        // how many are stale before the TTL finally forces a refetch.
        int staleReads = 0;
        int freshReads = 0;
        for (int t = 3; t <= 8; t++) {
            String value = nodeB.get("price:sku-42", t);
            boolean stale = value.equals("19.99");
            if (stale) {
                staleReads++;
            } else {
                freshReads++;
            }
            System.out.println("nodeB read at tick " + t + ": " + value + (stale ? " (stale)" : " (fresh)"));
        }
        System.out.println("nodeB reads across ticks 3-8: " + (staleReads + freshReads)
                + ", stale: " + staleReads + ", fresh: " + freshReads);
        System.out.println("Backend's actual current price: " + backend.read("price:sku-42"));
    }
}
