import java.util.HashMap;
import java.util.Map;

public class Main {
    static final double ALERT_THRESHOLD = 0.10;

    static class Counts {
        int total = 0;
        int failed = 0;
    }

    public static void main(String[] args) {
        // Every request is recorded with its tenant ID attached. TenantX's
        // 50 failing requests are counted against TenantX specifically, not
        // folded into one shared total with every other tenant's traffic.
        Map<String, Counts> byTenant = new HashMap<>();
        byTenant.put("TenantX", new Counts());
        for (int i = 0; i < 50; i++) {
            byTenant.get("TenantX").total++;
            byTenant.get("TenantX").failed++; // 100% failing
        }
        for (int t = 1; t <= 5; t++) {
            String tenantId = "Tenant" + t;
            byTenant.put(tenantId, new Counts());
            for (int i = 0; i < 190; i++) { // 5 healthy tenants x 190 = 950 total
                byTenant.get(tenantId).total++;
            }
        }

        System.out.println("Per-tenant error rates:");
        boolean anyAlertFired = false;
        for (Map.Entry<String, Counts> e : byTenant.entrySet()) {
            double rate = (double) e.getValue().failed / e.getValue().total;
            boolean alerts = rate > ALERT_THRESHOLD;
            anyAlertFired = anyAlertFired || alerts;
            if (alerts || e.getKey().equals("TenantX")) {
                System.out.println("  " + e.getKey() + ": " + String.format("%.0f", rate * 100)
                    + "% error rate, alert fired: " + alerts);
            }
        }
        System.out.println("  (5 other healthy tenants: 0% error rate each, no alert)");

        System.out.println();
        System.out.println(anyAlertFired
            ? "FIXED: TenantX's 100% error rate is visible and alerting on its own, specifically, "
              + "regardless of how many healthy requests from other tenants exist in the same window. "
              + "Tagging every metric with its tenant means an outage affecting one tenant can never be "
              + "diluted by traffic from tenants having a completely normal day."
            : "Unexpected -- investigate.");
    }
}
