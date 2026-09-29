import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WRONG-DIRECTION: Nova's catalog cache is split into one partition per tenant. In this
 * model, each tenant's items live in a separate LinkedHashMap instance, so one tenant's
 * accesses do not evict the other tenant's entries -- the cross-tenant eviction demonstrated
 * in the pressure scenario does not occur here. But the partition size is a naive fixed
 * 50/50 split of the same 100-slot total capacity, with no regard for either tenant's own
 * supplied historical working-set size. Tenant A's historical hot working set is 20 items,
 * comfortably under its 50-slot partition. Tenant B's is 80 items -- more than its 50-slot
 * partition can hold. Isolating the tenants from each other did not size either partition to
 * either tenant's own historical usage, so Tenant B now thrashes inside its own partition.
 */
public class Main {

    static final int PARTITION_CAPACITY_EACH = 50;

    public static void main(String[] args) {
        PartitionedCache tenantACache = new PartitionedCache(PARTITION_CAPACITY_EACH);
        PartitionedCache tenantBCache = new PartitionedCache(PARTITION_CAPACITY_EACH);

        System.out.println("-- Nova catalog cache, per-tenant partitioning, fixed 50/50 split --");
        System.out.println("Tenant A partition: " + PARTITION_CAPACITY_EACH + " slots");
        System.out.println("Tenant B partition: " + PARTITION_CAPACITY_EACH + " slots");
        System.out.println();

        // Tenant A's historical hot working set: 20 items, cycled through twice (steady, recurring access).
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < 20; i++) {
                tenantACache.access("tenantA-item-" + i);
            }
        }
        int tenantAHitsSecondPass = 0;
        for (int i = 0; i < 20; i++) {
            if (tenantACache.access("tenantA-item-" + i)) {
                tenantAHitsSecondPass++;
            }
        }
        int tenantAHitRatePct = (tenantAHitsSecondPass * 100) / 20;
        System.out.println("Tenant A (historical hot working set = 20 items, fits in its 50-slot partition):");
        System.out.println("  Hits on repeat pass: " + tenantAHitsSecondPass + "/20 (" + tenantAHitRatePct + "%)");
        System.out.println("  In this modeled run, Tenant A's partition is separate from Tenant B's, and");
        System.out.println("  Tenant B's accesses do not evict Tenant A's entries.");
        System.out.println();

        // Tenant B's historical hot working set: 80 items, cycled through twice (steady, recurring access) --
        // but its partition only holds 50, so it can't all stay resident.
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < 80; i++) {
                tenantBCache.access("tenantB-item-" + i);
            }
        }
        int tenantBHitsSecondPass = 0;
        for (int i = 0; i < 80; i++) {
            if (tenantBCache.access("tenantB-item-" + i)) {
                tenantBHitsSecondPass++;
            }
        }
        int tenantBHitRatePct = (tenantBHitsSecondPass * 100) / 80;
        System.out.println("Tenant B (historical hot working set = 80 items, only 50-slot partition available):");
        System.out.println("  Hits on repeat pass: " + tenantBHitsSecondPass + "/80 (" + tenantBHitRatePct + "%)");
        System.out.println("  Tenant B's own traffic never once touched Tenant A's items, and vice versa --");
        System.out.println("  the cross-tenant eviction seen in the shared-cache scenario does not occur in");
        System.out.println("  this modeled run. But Tenant B's partition is smaller than Tenant B's own");
        System.out.println("  historical working set, so Tenant B now thrashes inside its own fixed-size");
        System.out.println("  partition instead.");
    }

    /** One tenant's dedicated LRU partition -- no other tenant's traffic can touch it. */
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
