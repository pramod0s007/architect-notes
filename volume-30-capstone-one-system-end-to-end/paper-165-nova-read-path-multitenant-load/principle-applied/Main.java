import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PRINCIPLE-APPLIED: instead of splitting Nova's 100-slot catalog cache into a naive fixed
 * 50/50 partition, each tenant's partition is sized proportionally to that tenant's own
 * historically measured hot-working-set size (the same historical-usage-projection discipline
 * this series established in Volume 29 for cost allocation, applied here to cache capacity
 * instead of cost). Tenant A's historically measured hot working set is 20 items; Tenant B's
 * is 80 items. Allocating each tenant a partition proportional to its own historical size,
 * out of the same 100-slot total capacity, gives Tenant A 20 slots and Tenant B 80 slots --
 * exactly matching the historical working-set sizes supplied for this run, because those two
 * supplied sizes happen to sum exactly to the modeled 100-slot total capacity. Proportional
 * allocation does not in general reproduce each tenant's historical size this precisely --
 * it does so here only because of that sum coincidence.
 */
public class Main {

    static final int TOTAL_CAPACITY = 100;
    static final int TENANT_A_HISTORICAL_HOT_SET_SIZE = 20;
    static final int TENANT_B_HISTORICAL_HOT_SET_SIZE = 80;

    public static void main(String[] args) {
        int totalHistoricalSize = TENANT_A_HISTORICAL_HOT_SET_SIZE + TENANT_B_HISTORICAL_HOT_SET_SIZE;
        int tenantAPartitionSize = (TOTAL_CAPACITY * TENANT_A_HISTORICAL_HOT_SET_SIZE) / totalHistoricalSize;
        int tenantBPartitionSize = (TOTAL_CAPACITY * TENANT_B_HISTORICAL_HOT_SET_SIZE) / totalHistoricalSize;

        System.out.println("-- Nova catalog cache, usage-weighted per-tenant partitioning --");
        System.out.println("Total capacity: " + TOTAL_CAPACITY + " slots");
        System.out.println("Tenant A historical hot working set: " + TENANT_A_HISTORICAL_HOT_SET_SIZE
                + " items -> partition sized to " + tenantAPartitionSize + " slots");
        System.out.println("Tenant B historical hot working set: " + TENANT_B_HISTORICAL_HOT_SET_SIZE
                + " items -> partition sized to " + tenantBPartitionSize + " slots");
        System.out.println();

        PartitionedCache tenantACache = new PartitionedCache(tenantAPartitionSize);
        PartitionedCache tenantBCache = new PartitionedCache(tenantBPartitionSize);

        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < TENANT_A_HISTORICAL_HOT_SET_SIZE; i++) {
                tenantACache.access("tenantA-item-" + i);
            }
        }
        int tenantAHits = 0;
        for (int i = 0; i < TENANT_A_HISTORICAL_HOT_SET_SIZE; i++) {
            if (tenantACache.access("tenantA-item-" + i)) {
                tenantAHits++;
            }
        }
        int tenantAHitRatePct = (tenantAHits * 100) / TENANT_A_HISTORICAL_HOT_SET_SIZE;
        System.out.println("Tenant A: " + tenantAHits + "/" + TENANT_A_HISTORICAL_HOT_SET_SIZE
                + " hits on repeat pass (" + tenantAHitRatePct + "%).");

        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < TENANT_B_HISTORICAL_HOT_SET_SIZE; i++) {
                tenantBCache.access("tenantB-item-" + i);
            }
        }
        int tenantBHits = 0;
        for (int i = 0; i < TENANT_B_HISTORICAL_HOT_SET_SIZE; i++) {
            if (tenantBCache.access("tenantB-item-" + i)) {
                tenantBHits++;
            }
        }
        int tenantBHitRatePct = (tenantBHits * 100) / TENANT_B_HISTORICAL_HOT_SET_SIZE;
        System.out.println("Tenant B: " + tenantBHits + "/" + TENANT_B_HISTORICAL_HOT_SET_SIZE
                + " hits on repeat pass (" + tenantBHitRatePct + "%).");
        System.out.println();
        System.out.println("Sizing each partition from each tenant's own historically measured hot");
        System.out.println("working set, instead of a naive equal split, let both tenants' modeled");
        System.out.println("access patterns fit their own partitions in this modeled run. This");
        System.out.println("allocation is computed once from a historical snapshot; a tenant whose");
        System.out.println("usage grows or shrinks after that snapshot needs the allocation");
        System.out.println("recomputed against a fresh historical window, which this code does not");
        System.out.println("do on its own.");
    }

    /** One tenant's dedicated LRU partition, sized from that tenant's own historical usage. */
    static class PartitionedCache {
        private final LinkedHashMap<String, Boolean> store;

        PartitionedCache(int capacity) {
            this.store = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > capacity;
                }
            };
        }

        boolean access(String key) {
            if (store.containsKey(key)) {
                store.get(key);
                return true;
            }
            store.put(key, Boolean.TRUE);
            return false;
        }
    }
}
