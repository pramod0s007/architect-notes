import java.util.ArrayList;
import java.util.List;

/**
 * WRONG DIRECTION: this time a trade-off table is built across three candidate
 * architectures instead of picking one on cost alone. But the table scores every candidate
 * with a single generic weighted formula (a composite of cost, write capacity, and isolation)
 * modeled on a generic checklist, rather than checking Nova's own disclosed
 * constraints as pass/fail gates first.
 */
public class Main {
    public static void main(String[] args) {
        List<Architecture> candidates = new ArrayList<>();
        candidates.add(new Architecture("Shared-Schema, Row-Level Tenant Column", 1200.00, 1, 200, 800));
        candidates.add(new Architecture("Per-Tenant Dedicated Instance", 9400.00, 3, 50, 900));
        candidates.add(new Architecture("Per-Tenant Schema, Shared Cluster", 4800.00, 2, 120, 650));

        double minCost = Double.MAX_VALUE, maxCost = 0, maxWrite = 0, maxIso = 0;
        for (Architecture a : candidates) {
            minCost = Math.min(minCost, a.monthlyCostDollars);
            maxCost = Math.max(maxCost, a.monthlyCostDollars);
            maxWrite = Math.max(maxWrite, a.peakWritesPerSec);
            maxIso = Math.max(maxIso, a.isolationLevel);
        }

        System.out.println("-- Nova data-layer trade-off table: generic weighted checklist --");
        System.out.println("Weights: cost 50%, write capacity 30%, isolation 20% (illustrative weighting for this simulation)");
        System.out.println();

        Architecture winner = null;
        double bestScore = -1;
        for (Architecture a : candidates) {
            double costScore = (maxCost - a.monthlyCostDollars) / (maxCost - minCost);
            double writeScore = a.peakWritesPerSec / maxWrite;
            double isoScore = a.isolationLevel / maxIso;
            double weighted = costScore * 0.5 + writeScore * 0.3 + isoScore * 0.2;

            System.out.printf("  %s%n", a.name);
            System.out.printf("    cost=$%.2f (score %.3f), writes=%d/sec (score %.3f), isolation=%d (score %.3f)%n",
                    a.monthlyCostDollars, costScore, a.peakWritesPerSec, writeScore, a.isolationLevel, isoScore);
            System.out.printf("    weighted composite score: %.4f%n", weighted);

            if (weighted > bestScore) {
                bestScore = weighted;
                winner = a;
            }
        }

        System.out.println();
        System.out.println("Highest weighted composite: " + winner.name + " (score " + String.format("%.4f", bestScore) + ")");
        System.out.println();
        System.out.println("Checking the winner against Nova's own disclosed constraints, after the fact:");
        boolean meetsRequiredIsolation = winner.isolationLevel >= 2;
        System.out.println("  Isolation level " + winner.isolationLevel + " >= required 2 (compliance regime)? " + meetsRequiredIsolation);
        System.out.println();
        System.out.println("Result: a trade-off table was built and scored across three inputs, but isolation was one weighted");
        System.out.println("input among three (worth 20%) rather than a pass/fail requirement. A low");
        System.out.println("isolation score can still win if it is outweighed by a high cost score --");
        System.out.println("exactly what happened here. The winner still fails Nova's own disclosed");
        System.out.println("compliance requirement, the same failure as picking on cost alone.");
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
