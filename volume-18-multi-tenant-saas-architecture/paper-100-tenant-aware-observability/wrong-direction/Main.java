import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    static final int MAX_SERIES = 5; // the metrics backend's real, finite series capacity
    static final Set<String> registeredSeries = new LinkedHashSet<>();

    static class Counts {
        int total = 0;
        int failed = 0;
    }

    // Overcorrection: metrics ARE tagged per tenant -- this isn't the
    // pressure case's mistake, the data model is correct. But a real
    // metrics backend has a finite number of distinct time series it can
    // hold, and nothing here accounts for that limit -- a new tenant's
    // series is simply rejected once capacity is full.
    static boolean tryRegisterSeries(String tenantId) {
        if (registeredSeries.contains(tenantId)) return true;
        if (registeredSeries.size() >= MAX_SERIES) return false;
        registeredSeries.add(tenantId);
        return true;
    }

    public static void main(String[] args) {
        Map<String, Counts> byTenant = new HashMap<>();

        // Five healthy tenants register first and fill the backend's entire
        // series capacity.
        for (int t = 1; t <= 5; t++) {
            String tenantId = "Tenant" + t;
            if (tryRegisterSeries(tenantId)) {
                byTenant.put(tenantId, new Counts());
                for (int i = 0; i < 190; i++) byTenant.get(tenantId).total++;
            }
        }
        System.out.println("Series capacity: " + MAX_SERIES + ". Registered after 5 healthy tenants: " + registeredSeries.size());

        // TenantX -- the one actually having an outage -- tries to register
        // its series after capacity is already full.
        boolean tenantXRegistered = tryRegisterSeries("TenantX");
        System.out.println("TenantX attempts to register its series -- accepted: " + tenantXRegistered);

        if (tenantXRegistered) {
            byTenant.put("TenantX", new Counts());
        }
        for (int i = 0; i < 50; i++) {
            if (byTenant.containsKey("TenantX")) {
                byTenant.get("TenantX").total++;
                byTenant.get("TenantX").failed++;
            }
            // if TenantX's series was rejected, these 50 failing requests
            // have nowhere to be recorded at all -- they are simply dropped.
        }

        System.out.println();
        boolean tenantXVisible = byTenant.containsKey("TenantX");
        System.out.println("TenantX has any recorded metrics at all: " + tenantXVisible);

        System.out.println();
        System.out.println(!tenantXVisible
            ? "CONFIRMED FAILURE: TenantX's 50 failing requests -- a complete outage -- were never "
              + "recorded anywhere, because its series was rejected the moment it tried to register, "
              + "after 5 healthy tenants had already filled every available slot. The metrics ARE "
              + "tenant-tagged, exactly like the fix -- but the backend's finite series capacity means "
              + "tagging alone doesn't guarantee visibility. The tenant having the actual incident is "
              + "silently invisible again, through a completely different mechanism than the original "
              + "problem."
            : "Unexpected -- investigate.");
    }
}
