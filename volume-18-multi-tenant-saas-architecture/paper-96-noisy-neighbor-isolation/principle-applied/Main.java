import java.util.HashMap;
import java.util.Map;

public class Main {
    static final int SHARED_CAPACITY = 100;

    public static void main(String[] args) {
        Map<String, Integer> requested = new HashMap<>();
        requested.put("TenantA", 150);
        requested.put("TenantB", 10);

        int numTenants = requested.size();
        int guaranteedQuota = SHARED_CAPACITY / numTenants; // 50 each

        // Step 1: every tenant is granted up to its guaranteed quota.
        Map<String, Integer> processed = new HashMap<>();
        int usedByBaseline = 0;
        for (Map.Entry<String, Integer> e : requested.entrySet()) {
            int granted = Math.min(e.getValue(), guaranteedQuota);
            processed.put(e.getKey(), granted);
            usedByBaseline += granted;
        }

        // Step 2: any capacity a tenant didn't need is reclaimed and offered
        // to tenants that still want more, up to total system capacity.
        int leftover = SHARED_CAPACITY - usedByBaseline;
        for (Map.Entry<String, Integer> e : requested.entrySet()) {
            String tenant = e.getKey();
            int stillWants = e.getValue() - processed.get(tenant);
            if (stillWants > 0 && leftover > 0) {
                int extra = Math.min(stillWants, leftover);
                processed.put(tenant, processed.get(tenant) + extra);
                leftover -= extra;
            }
        }

        System.out.println("Shared capacity: " + SHARED_CAPACITY + ", guaranteed quota per tenant: " + guaranteedQuota);
        System.out.println("Tenant A processed: " + processed.get("TenantA") + " of 150 requested");
        System.out.println("Tenant B processed: " + processed.get("TenantB") + " of 10 requested");
        System.out.println("Total processed: " + (processed.get("TenantA") + processed.get("TenantB")) + " of " + SHARED_CAPACITY + " capacity");

        System.out.println();
        System.out.println(processed.get("TenantB") == 10
            ? "FIXED: Tenant B got 100% of what it legitimately needed, guaranteed, regardless of how "
              + "large Tenant A's burst was. Tenant A was still allowed to use the capacity Tenant B "
              + "genuinely didn't need -- full system utilization, and B's small, well-behaved usage was "
              + "never at the mercy of A's volume."
            : "Unexpected -- investigate.");
    }
}
