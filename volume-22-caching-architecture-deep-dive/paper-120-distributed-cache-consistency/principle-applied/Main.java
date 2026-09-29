import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates that write-triggered invalidation, broadcast to every node
 * before the write returns, eliminates staleness entirely -- including the
 * read-your-own-write case that a TTL cannot solve regardless of how short
 * the TTL is set.
 */
public class Main {
    public static void main(String[] args) {
        Backend backend = new Backend();
        List<CacheNode> registry = new ArrayList<>();
        CacheNode nodeA = new CacheNode("nodeA", backend, registry);
        CacheNode nodeB = new CacheNode("nodeB", backend, registry);

        backend.write("price:sku-42", "19.99");

        System.out.println("Warm read on nodeA: " + nodeA.get("price:sku-42"));
        System.out.println("Warm read on nodeB: " + nodeB.get("price:sku-42"));

        // A price update lands on nodeA. The write broadcasts an
        // invalidation to nodeB (and nodeA itself) before returning.
        nodeA.write("price:sku-42", "24.99");
        System.out.println("nodeA wrote new price 24.99 (invalidation broadcast to all nodes)");

        String immediateA = nodeA.get("price:sku-42");
        System.out.println("Read on nodeA immediately after write: " + immediateA);

        // The very next read on nodeB, with no delay at all, already sees
        // the fresh value -- read-your-own-write holds.
        String immediateB = nodeB.get("price:sku-42");
        System.out.println("Read-your-own-write attempt on nodeB (immediately after write): " + immediateB
                + (immediateB.equals("24.99") ? " -- fresh, read-your-own-write holds" : " -- STALE"));

        // Simulate 10 subsequent reads on nodeB with no further writes --
        // every one should be fresh, with zero stale reads.
        int staleReads = 0;
        int totalReads = 10;
        for (int i = 0; i < totalReads; i++) {
            String value = nodeB.get("price:sku-42");
            if (value.equals("19.99")) {
                staleReads++;
            }
        }
        System.out.println("nodeB reads after write: " + totalReads
                + ", stale: " + staleReads);
        System.out.println("Backend's actual current price: " + backend.read("price:sku-42"));
    }
}
