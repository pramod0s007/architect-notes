/**
 * Demonstrates permanent cache staleness with no invalidation mechanism.
 *
 * Two independent cache nodes (nodeA, nodeB) sit in front of the same backend,
 * simulating two instances of the same service behind a load balancer, each
 * with its own local in-memory cache. A write through nodeA updates the
 * backend and nodeA's own cache, but nodeB has no way to learn about it.
 */
public class Main {
    public static void main(String[] args) {
        Backend backend = new Backend();
        CacheNode nodeA = new CacheNode("nodeA", backend);
        CacheNode nodeB = new CacheNode("nodeB", backend);

        backend.write("price:sku-42", "19.99");

        // Both nodes warm their caches by reading the current price once.
        String warmA = nodeA.get("price:sku-42");
        String warmB = nodeB.get("price:sku-42");
        System.out.println("Warm read on nodeA: " + warmA);
        System.out.println("Warm read on nodeB: " + warmB);

        // A price update comes in through nodeA (e.g. an admin hits the
        // instance nodeA happens to be routed to).
        nodeA.write("price:sku-42", "24.99");
        System.out.println("nodeA wrote new price: 24.99");

        // nodeA immediately reflects the new price -- its own cache was updated.
        String afterWriteA = nodeA.get("price:sku-42");
        System.out.println("Read on nodeA immediately after write: " + afterWriteA);

        // nodeB never received the update. Simulate 10 subsequent read
        // requests landing on nodeB over time -- every single one is stale,
        // because nothing ever tells nodeB to drop or refresh its entry.
        int staleReads = 0;
        int totalReads = 10;
        for (int i = 0; i < totalReads; i++) {
            String value = nodeB.get("price:sku-42");
            if (value.equals("19.99")) {
                staleReads++;
            }
        }
        System.out.println("nodeB reads after write: " + totalReads
                + ", stale (still showing 19.99): " + staleReads);
        System.out.println("Backend's actual current price: " + backend.read("price:sku-42"));
    }
}
