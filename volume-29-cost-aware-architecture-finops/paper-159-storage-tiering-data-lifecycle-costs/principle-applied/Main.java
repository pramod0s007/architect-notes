import java.util.*;

/**
 * PRINCIPLE-APPLIED SCENARIO: per-object, cost-projected storage tiering.
 *
 * Rather than tiering by age or by recency of the single most recent access,
 * this policy projects each object's own total monthly cost -- storage cost plus
 * expected retrieval cost -- in each available tier, using that object's own
 * observed average access frequency, and places the object in whichever tier
 * minimizes that projected total. An object with too little access history to
 * project confidently (fewer than MIN_HISTORY_MONTHS of observations) is not
 * tiered off that thin sample at all -- it is held at a conservative default
 * tier (HOT) until enough history accumulates to project with.
 */
public class Main {

    enum Tier {
        HOT(0.023, 0.0),
        COOL(0.0125, 0.01),
        ARCHIVE(0.00099, 0.02);

        final double storagePerGBMonth;
        final double retrievalPerGB;

        Tier(double storagePerGBMonth, double retrievalPerGB) {
            this.storagePerGBMonth = storagePerGBMonth;
            this.retrievalPerGB = retrievalPerGB;
        }
    }

    static final int MIN_HISTORY_MONTHS = 3;
    static final Tier DEFAULT_TIER_FOR_THIN_HISTORY = Tier.HOT; // conservative: assume it may need frequent access until proven otherwise

    static class StorageObject {
        String name;
        double sizeGB;
        double observedAccessesPerMonth; // this object's own historical average retrieval count
        int monthsOfHistory;

        StorageObject(String name, double sizeGB, double observedAccessesPerMonth, int monthsOfHistory) {
            this.name = name;
            this.sizeGB = sizeGB;
            this.observedAccessesPerMonth = observedAccessesPerMonth;
            this.monthsOfHistory = monthsOfHistory;
        }
    }

    static double monthlyCost(StorageObject o, Tier tier) {
        return o.sizeGB * tier.storagePerGBMonth + o.observedAccessesPerMonth * o.sizeGB * tier.retrievalPerGB;
    }

    static Tier cheapestTier(StorageObject o) {
        Tier best = Tier.HOT;
        double bestCost = monthlyCost(o, Tier.HOT);
        for (Tier t : Tier.values()) {
            double cost = monthlyCost(o, t);
            if (cost < bestCost) {
                best = t;
                bestCost = cost;
            }
        }
        return best;
    }

    public static void main(String[] args) {
        List<StorageObject> objects = new ArrayList<>();
        objects.add(new StorageObject("compliance-reports", 500, 2.0, 12));
        objects.add(new StorageObject("cold-logs", 2000, 0.0, 12));
        objects.add(new StorageObject("occasional-audit-export", 300, 0.1, 12));
        // new-feature-export: pre-launch, only 1 month of history so far, zero
        // observed access -- but the feature it backs has not launched yet, so
        // "zero access this month" is not evidence the object is genuinely cold.
        objects.add(new StorageObject("new-feature-export", 100, 0.0, 1));

        System.out.println("-- Per-object projected total cost (storage + expected retrieval) in each tier --");
        double totalOptimalCost = 0.0;
        for (StorageObject o : objects) {
            System.out.printf("%-26s size=%6.0fGB observed-accesses=%.1f/mo history=%d month(s)%n",
                    o.name, o.sizeGB, o.observedAccessesPerMonth, o.monthsOfHistory);
            for (Tier t : Tier.values()) {
                System.out.printf("    %-8s $%.3f/month%n", t, monthlyCost(o, t));
            }

            Tier chosen;
            if (o.monthsOfHistory < MIN_HISTORY_MONTHS) {
                chosen = DEFAULT_TIER_FOR_THIN_HISTORY;
                System.out.printf("    -> INSUFFICIENT_HISTORY (need >= %d months, have %d) -- held at conservative default %s, $%.3f/month%n",
                        MIN_HISTORY_MONTHS, o.monthsOfHistory, chosen, monthlyCost(o, chosen));
            } else {
                chosen = cheapestTier(o);
                System.out.printf("    -> chosen tier: %s, $%.3f/month (cheapest projected total for this object's own access history)%n",
                        chosen, monthlyCost(o, chosen));
            }
            totalOptimalCost += monthlyCost(o, chosen);
        }
        System.out.printf("Total under per-object, cost-projected tiering: $%.3f/month%n", totalOptimalCost);

        System.out.println();
        System.out.println("-- What the thin-history guard is protecting against --");
        StorageObject newFeature = objects.get(3);
        Tier naiveChoice = cheapestTier(newFeature);
        double naiveCost = monthlyCost(newFeature, naiveChoice);
        double guardedCost = monthlyCost(newFeature, DEFAULT_TIER_FOR_THIN_HISTORY);
        System.out.printf("new-feature-export: projecting off its single, pre-launch month of zero-access history alone would pick %s ($%.3f/month) -- the guard instead holds it at %s ($%.3f/month) until it has %d months of access history to project from%n",
                naiveChoice, naiveCost, DEFAULT_TIER_FOR_THIN_HISTORY, guardedCost, MIN_HISTORY_MONTHS);
    }
}
