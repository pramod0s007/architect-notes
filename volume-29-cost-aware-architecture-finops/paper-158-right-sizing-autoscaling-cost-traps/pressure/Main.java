import java.util.*;

public class Main {

    static final double PRICE_PER_VCPU_MONTH = 30.0; // $/vCPU/month, illustrative flat rate

    static double monthlyCost(double requestMillicores, int replicas) {
        return (requestMillicores / 1000.0) * PRICE_PER_VCPU_MONTH * replicas;
    }

    static double average(double[] usage) {
        double sum = 0;
        for (double u : usage) sum += u;
        return sum / usage.length;
    }

    static double max(double[] usage) {
        double m = usage[0];
        for (double u : usage) if (u > m) m = u;
        return m;
    }

    public static void main(String[] args) {
        // checkout: 30 days of this workload's own peak per-replica CPU demand (millicores), each day's
        // value being the highest demand observed during that day's busiest window, not a daily average --
        // whole-millicore samples. Two flash-sale days (indices 10, 22) spike to 900m; every other day's
        // peak sits in a 195-250m baseline range.
        double[] checkoutUsage = {
            210, 225, 198, 240, 205, 215, 230, 200, 220, 235,
            900, 210, 225, 198, 240, 205, 215, 230, 200, 220,
            235, 210, 900, 225, 198, 240, 205, 215, 230, 220
        };
        int replicas = 4;
        double currentRequest = 1000.0; // existing, overprovisioned static CPU request per replica

        System.out.println("-- Before rightsizing --");
        double currentCost = monthlyCost(currentRequest, replicas);
        System.out.printf("checkout: request=%.0fm x %d replicas -> $%.2f/month%n",
                currentRequest, replicas, currentCost);

        double avgUsage = average(checkoutUsage);
        double rightsizedRequest = Math.round(avgUsage / 10.0) * 10.0; // rightsize to observed average
        double newCost = monthlyCost(rightsizedRequest, replicas);
        double monthlySavings = currentCost - newCost;

        System.out.println();
        System.out.println("-- Rightsizing to the observed 30-day average usage --");
        System.out.printf("checkout: average usage = %.1fm -> new request = %.0fm%n", avgUsage, rightsizedRequest);
        System.out.printf("checkout: request=%.0fm x %d replicas -> $%.2f/month (saves $%.2f/month)%n",
                rightsizedRequest, replicas, newCost, monthlySavings);

        // Replay the next flash-sale day against the new, smaller capacity.
        double spikeDemand = max(checkoutUsage); // 900m, the same spike level already seen twice in the history
        System.out.println();
        System.out.println("-- Next flash-sale day arrives, demand repeats the historical spike --");
        System.out.printf("checkout: demand=%.0fm, capacity=%.0fm%n", spikeDemand, rightsizedRequest);
        if (spikeDemand > rightsizedRequest) {
            System.out.println("THROTTLED: demand exceeds the rightsized capacity -- requests queue, checkout latency spikes,");
            System.out.println("some checkout attempts time out during the flash sale's peak traffic window.");
        } else {
            System.out.println("OK: capacity covers demand.");
        }

        double incidentCost = 4800.0; // illustrative estimate: ~45 minutes of degraded checkout during peak traffic
        System.out.println();
        System.out.println("-- Net result for the month this spike recurs --");
        System.out.printf("Monthly savings from rightsizing: $%.2f%n", monthlySavings);
        System.out.printf("Estimated cost of the single incident above: $%.2f%n", incidentCost);
        System.out.printf("Net: rightsizing this workload to its average usage cost $%.2f more than it saved,%n",
                incidentCost - monthlySavings);
        System.out.println("because the average usage never told anyone how high this workload's demand actually goes.");
    }
}
