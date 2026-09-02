import java.util.HashMap;
import java.util.Map;

public class Main {
    static final int SHARED_CAPACITY = 100;

    public static void main(String[] args) {
        Map<String, Integer> requested = new HashMap<>();
        requested.put("TenantA", 150);
        requested.put("TenantB", 10);

        // Overcorrection: per-tenant quotas exist now -- this isn't the
        // pressure case's mistake, Tenant B is structurally guaranteed its
        // share. But the quota is a RIGID, fixed cap with no mechanism to
        // reclaim a tenant's unused allocation and offer it to someone who
        // still wants more.
        int numTenants = requested.size();
        int fixedQuota = SHARED_CAPACITY / numTenants; // 50 each, hard cap, no reclaim

        Map<String, Integer> processed = new HashMap<>();
        for (Map.Entry<String, Integer> e : requested.entrySet()) {
            processed.put(e.getKey(), Math.min(e.getValue(), fixedQuota));
        }

        int totalProcessed = processed.get("TenantA") + processed.get("TenantB");
        int idleCapacity = SHARED_CAPACITY - totalProcessed;

        System.out.println("Shared capacity: " + SHARED_CAPACITY + ", fixed quota per tenant: " + fixedQuota + " (no reclaim)");
        System.out.println("Tenant A processed: " + processed.get("TenantA") + " of 150 requested");
        System.out.println("Tenant B processed: " + processed.get("TenantB") + " of 10 requested");
        System.out.println("Total processed: " + totalProcessed + " of " + SHARED_CAPACITY + " capacity, idle: " + idleCapacity);

        System.out.println();
        System.out.println(idleCapacity > 0 && processed.get("TenantA") < 150
            ? "CONFIRMED FAILURE: Tenant B is fully protected, exactly as intended -- but " + idleCapacity
              + " units of system capacity sit idle every window, because Tenant B's unused "
              + idleCapacity + "-unit allocation is locked to Tenant B and can never be offered to "
              + "Tenant A, even though A "
              + "has real, legitimate demand for it. A paying customer is throttled below what the "
              + "system can actually deliver, and the system runs under-utilized at the same time -- "
              + "both problems, simultaneously, because the quota can only shrink a tenant's ceiling, "
              + "never reallocate what a different tenant isn't using."
            : "Unexpected -- investigate.");
    }
}
