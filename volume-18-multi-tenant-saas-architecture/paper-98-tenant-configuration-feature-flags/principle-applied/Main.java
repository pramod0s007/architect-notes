import java.util.HashMap;
import java.util.Map;

public class Main {
    // Configuration is data, keyed by tenant, resolved at request time.
    // Enabling it for one tenant is a single entry in this map -- nothing
    // about any other tenant's entry (or absence of one) changes.
    static final Map<String, Boolean> newCheckoutFlowByTenant = new HashMap<>();

    static boolean isNewCheckoutEnabled(String tenantId) {
        return newCheckoutFlowByTenant.getOrDefault(tenantId, false);
    }

    public static void main(String[] args) {
        System.out.println("Before rollout -- TenantA: " + isNewCheckoutEnabled("TenantA")
            + ", TenantB: " + isNewCheckoutEnabled("TenantB") + ", TenantC: " + isNewCheckoutEnabled("TenantC"));

        System.out.println();
        System.out.println("An engineer enables the new checkout flow for TenantA only, as a beta:");
        newCheckoutFlowByTenant.put("TenantA", true);

        System.out.println();
        System.out.println("After enabling -- TenantA: " + isNewCheckoutEnabled("TenantA")
            + ", TenantB: " + isNewCheckoutEnabled("TenantB") + ", TenantC: " + isNewCheckoutEnabled("TenantC"));

        System.out.println();
        System.out.println(isNewCheckoutEnabled("TenantA") && !isNewCheckoutEnabled("TenantB") && !isNewCheckoutEnabled("TenantC")
            ? "FIXED: only TenantA sees the new checkout flow. TenantB and TenantC's requests resolve "
              + "against their own, unset entries in the same map and correctly default to false -- "
              + "enabling one tenant's config never touches any other tenant's, because each tenant has "
              + "its own independent entry."
            : "Unexpected -- investigate.");
    }
}
