/**
 * PRESSURE: Nova's operational cost is tracked as a single running total, incremented by every
 * operation across every tenant and every mechanism (cache, resilience, security), with nothing
 * else recorded alongside it. When the total cost rises, nothing in this model records which
 * tenant or which mechanism contributed which amount -- the only retained structured cost state
 * is the running total itself.
 */
public class Main {

    public static void main(String[] args) {
        long totalCost = 0;

        System.out.println("-- Nova cost tracking: single running total, no per-tenant/mechanism breakdown --");
        System.out.println();

        // Operation 1: tenant-a, cache mechanism, cache miss triggers a modeled downstream DB call.
        System.out.println("Operation 1 -- tenant-a, CACHE mechanism, cache miss (modeled cost: 40):");
        totalCost += recordOperation(40);
        System.out.println("  Running total cost: " + totalCost);
        System.out.println();

        // Operation 2: tenant-a, resilience mechanism, circuit breaker open after repeated failures.
        System.out.println("Operation 2 -- tenant-a, RESILIENCE mechanism, breaker open, after repeated failures (modeled cost: 5):");
        totalCost += recordOperation(5);
        System.out.println("  Running total cost: " + totalCost);
        System.out.println();

        // Operation 3: tenant-b, cache mechanism, cache hit, cheap.
        System.out.println("Operation 3 -- tenant-b, CACHE mechanism, cache hit (modeled cost: 2):");
        totalCost += recordOperation(2);
        System.out.println("  Running total cost: " + totalCost);
        System.out.println();

        System.out.println("Result: final total cost = " + totalCost + ". Nothing in this model records which");
        System.out.println("tenant or mechanism contributed how much of that total -- if this total");
        System.out.println("spikes, there is no recorded basis in this model for attributing the spike to any");
        System.out.println("specific tenant or mechanism.");
    }

    /** Adds the modeled cost of one operation to the running total; records nothing else. */
    static long recordOperation(long modeledCost) {
        return modeledCost;
    }
}
