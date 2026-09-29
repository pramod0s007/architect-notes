import java.util.ArrayList;
import java.util.List;

/**
 * PRINCIPLE APPLIED: the same three candidate architectures, but Nova's own disclosed
 * constraints (isolation level, concurrent-tenant count, peak write capacity, monthly budget
 * ceiling) are applied first as pass/fail gates. Only a candidate that clears every gate is
 * ranked at all; ranking by a secondary preference (lowest cost) only happens among survivors.
 * This is the architecture Papers 163-168 build on for the rest of Nova.
 */
public class Main {
    // Nova's own disclosed hard constraints, established once for this system.
    static final int REQUIRED_ISOLATION_LEVEL = 2;   // compliance regime requires schema-level tenant separation
    static final int REQUIRED_MIN_TENANTS = 50;
    static final int REQUIRED_MIN_PEAK_WRITES_PER_SEC = 500;
    static final double BUDGET_CEILING_DOLLARS = 6000.00;

    public static void main(String[] args) {
        List<Architecture> candidates = new ArrayList<>();
        candidates.add(new Architecture("Shared-Schema, Row-Level Tenant Column", 1200.00, 1, 200, 800));
        candidates.add(new Architecture("Per-Tenant Dedicated Instance", 9400.00, 3, 50, 900));
        candidates.add(new Architecture("Per-Tenant Schema, Shared Cluster", 4800.00, 2, 120, 650));

        System.out.println("-- Nova data-layer trade-off table: Nova's own constraints as pass/fail gates --");
        System.out.println("Gates: isolation >= " + REQUIRED_ISOLATION_LEVEL
                + ", tenants >= " + REQUIRED_MIN_TENANTS
                + ", peak writes/sec >= " + REQUIRED_MIN_PEAK_WRITES_PER_SEC
                + ", monthly cost <= $" + String.format("%.2f", BUDGET_CEILING_DOLLARS));
        System.out.println();

        List<Architecture> survivors = new ArrayList<>();
        for (Architecture a : candidates) {
            boolean isolationGate = a.isolationLevel >= REQUIRED_ISOLATION_LEVEL;
            boolean tenantGate = a.maxConcurrentTenants >= REQUIRED_MIN_TENANTS;
            boolean writeGate = a.peakWritesPerSec >= REQUIRED_MIN_PEAK_WRITES_PER_SEC;
            boolean budgetGate = a.monthlyCostDollars <= BUDGET_CEILING_DOLLARS;
            boolean passesAll = isolationGate && tenantGate && writeGate && budgetGate;

            System.out.println("  " + a.name);
            System.out.println("    isolation gate: " + a.isolationLevel + " -> " + (isolationGate ? "PASS" : "FAIL"));
            System.out.println("    tenant-count gate: " + a.maxConcurrentTenants + " -> " + (tenantGate ? "PASS" : "FAIL"));
            System.out.println("    write-capacity gate: " + a.peakWritesPerSec + "/sec -> " + (writeGate ? "PASS" : "FAIL"));
            System.out.printf("    budget gate: $%.2f -> %s%n", a.monthlyCostDollars, budgetGate ? "PASS" : "FAIL");
            System.out.println("    overall: " + (passesAll ? "SURVIVES all gates" : "eliminated"));

            if (passesAll) {
                survivors.add(a);
            }
        }

        System.out.println();
        if (survivors.isEmpty()) {
            System.out.println("Result: no candidate survives all of Nova's own disclosed constraints.");
            System.out.println("A new candidate would need to be evaluated -- none of the three considered here work.");
            return;
        }

        Architecture recommended = survivors.get(0);
        for (Architecture a : survivors) {
            if (a.monthlyCostDollars < recommended.monthlyCostDollars) {
                recommended = a;
            }
        }

        System.out.println("Survivors of all four gates: " + survivors.size() + " of " + candidates.size());
        System.out.println("Recommended architecture: " + recommended.name);
        System.out.printf("  Monthly cost: $%.2f (lowest cost among the survivors)%n", recommended.monthlyCostDollars);
        System.out.println();
        System.out.println("Result: unlike the cost-only pick and the generic-weighted table, this");
        System.out.println("recommendation is scored against Nova's own disclosed constraints as hard");
        System.out.println("gates first, so a candidate that fails Nova's compliance isolation");
        System.out.println("requirement or its own budget ceiling cannot win regardless of how it");
        System.out.println("scores on any other dimension. This is the architecture Papers 163-168");
        System.out.println("build on for the rest of Nova: Per-Tenant Schema, Shared Cluster.");
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
