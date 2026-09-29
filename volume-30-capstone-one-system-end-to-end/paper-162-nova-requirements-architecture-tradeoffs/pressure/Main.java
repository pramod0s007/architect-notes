import java.util.ArrayList;
import java.util.List;

/**
 * PRESSURE: Nova (a multi-tenant B2B order/checkout platform, the single system this whole
 * volume follows) needs a data-layer architecture. A single "recommended architecture" is
 * picked because it is the cheapest option on the vendor's own comparison page, with no
 * table scored against Nova's own disclosed constraints at all.
 */
public class Main {
    public static void main(String[] args) {
        Architecture picked = new Architecture(
                "Shared-Schema, Row-Level Tenant Column",
                1200.00, 1 /* isolation level: 1 = row-level column separation */,
                200 /* max concurrent tenants supported */,
                800 /* peak order-writes/sec supported */);

        System.out.println("-- Nova data-layer pick, no trade-off table --");
        System.out.println("Recommended architecture: " + picked.name);
        System.out.printf("  Monthly cost: $%.2f (cheapest option evaluated)%n", picked.monthlyCostDollars);
        System.out.println("  Reasoning: lowest monthly cost among the options looked at.");
        System.out.println();
        System.out.println("Nova's own disclosed constraints (recorded separately, not consulted here):");
        System.out.println("  - Compliance regime requires schema-level tenant separation at isolation");
        System.out.println("    level 2 (schema-per-tenant) or higher -- row-level column separation");
        System.out.println("    (isolation level 1) does not satisfy this regime's audit requirement.");
        System.out.println("  - Must support >= 50 concurrent tenants.");
        System.out.println("  - Must support >= 500 order-writes/sec at peak (seasonal burst).");
        System.out.println("  - Monthly budget ceiling: $6,000.00.");
        System.out.println();
        boolean meetsRequiredIsolation = picked.isolationLevel >= 2;
        System.out.println("Checking the picked architecture against those constraints after the fact:");
        System.out.println("  Isolation level " + picked.isolationLevel + " >= required 2? " + meetsRequiredIsolation);
        System.out.println();
        System.out.println("Result: the picked architecture does not meet Nova's own disclosed");
        System.out.println("compliance isolation requirement. This was never checked before picking it,");
        System.out.println("because no table was built against Nova's own constraints -- only against");
        System.out.println("monthly cost.");
    }

    static class Architecture {
        final String name;
        final double monthlyCostDollars;
        final int isolationLevel;
        final int maxConcurrentTenants;
        final int peakWritesPerSec;

        Architecture(String name, double monthlyCostDollars, int isolationLevel,
                     int maxConcurrentTenants, int peakWritesPerSec) {
            this.name = name;
            this.monthlyCostDollars = monthlyCostDollars;
            this.isolationLevel = isolationLevel;
            this.maxConcurrentTenants = maxConcurrentTenants;
            this.peakWritesPerSec = peakWritesPerSec;
        }
    }
}
