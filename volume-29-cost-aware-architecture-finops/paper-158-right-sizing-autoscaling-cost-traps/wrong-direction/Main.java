import java.util.*;

public class Main {

    static final double PRICE_PER_VCPU_MONTH = 30.0; // $/vCPU/month, illustrative flat rate
    static final double BLANKET_HEADROOM_MULTIPLIER = 2.5; // applied uniformly to every workload's average

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
        // Same 30-day checkout history as the pressure scenario (each sample is that day's own peak
        // per-replica CPU demand, not a daily average): two flash-sale days spike to 900m.
        double[] checkoutUsage = {
            210, 225, 198, 240, 205, 215, 230, 200, 220, 235,
            900, 210, 225, 198, 240, 205, 215, 230, 200, 220,
            235, 210, 900, 225, 198, 240, 205, 215, 230, 220
        };
        int checkoutReplicas = 4;

        // A second, unrelated workload with steady, low-variance usage -- no spikes at all.
        double[] searchUsage = {
            200, 202, 198, 205, 199, 203, 201, 204, 198, 206,
            200, 202, 199, 205, 201, 203, 198, 206, 200, 202,
            199, 205, 201, 203, 198, 206, 200, 202, 199, 205
        };
        int searchReplicas = 6;

        System.out.println("-- Fix: apply a flat " + BLANKET_HEADROOM_MULTIPLIER + "x headroom multiplier to every workload's average usage --");

        double checkoutAvg = average(checkoutUsage);
        double checkoutRequest = Math.round(checkoutAvg * BLANKET_HEADROOM_MULTIPLIER / 10.0) * 10.0;
        double checkoutCost = monthlyCost(checkoutRequest, checkoutReplicas);
        System.out.printf("checkout: average=%.1fm -> request=%.0fm x %d replicas -> $%.2f/month%n",
                checkoutAvg, checkoutRequest, checkoutReplicas, checkoutCost);

        double searchAvg = average(searchUsage);
        double searchRequest = Math.round(searchAvg * BLANKET_HEADROOM_MULTIPLIER / 10.0) * 10.0;
        double searchCost = monthlyCost(searchRequest, searchReplicas);
        System.out.printf("search:   average=%.1fm -> request=%.0fm x %d replicas -> $%.2f/month%n",
                searchAvg, searchRequest, searchReplicas, searchCost);

        // Replay the same flash-sale day against checkout's new, larger-but-still-flat-derived capacity.
        double spikeDemand = max(checkoutUsage);
        System.out.println();
        System.out.println("-- Next flash-sale day arrives, demand repeats the historical spike --");
        System.out.printf("checkout: demand=%.0fm, capacity=%.0fm%n", spikeDemand, checkoutRequest);
        if (spikeDemand > checkoutRequest) {
            System.out.println("STILL THROTTLED: a flat multiplier of the average is still an average-derived number --");
            System.out.println("2.5x an average whose multiplier was never calibrated to this workload's own peak is still short of the peak.");
        } else {
            System.out.println("OK: capacity covers demand.");
        }

        // Show what search's own usage actually required, versus what the blanket policy charged it for.
        double searchOwnPeakNeed = Math.round(max(searchUsage) * 1.10 / 10.0) * 10.0; // its own peak + 10% margin
        double searchOwnCost = monthlyCost(searchOwnPeakNeed, searchReplicas);
        double searchWaste = searchCost - searchOwnCost;

        System.out.println();
        System.out.println("-- What the blanket policy actually cost search, a workload with no spikes at all --");
        System.out.printf("search: blanket-policy request=%.0fm -> $%.2f/month%n", searchRequest, searchCost);
        System.out.printf("search: its own peak usage (%.0fm) plus a 10%% margin would need only %.0fm -> $%.2f/month%n",
                max(searchUsage), searchOwnPeakNeed, searchOwnCost);
        System.out.printf("search: blanket policy overspends by $%.2f/month on a workload that never needed the buffer%n",
                searchWaste);

        System.out.println();
        System.out.println("-- Net result --");
        System.out.println("The same flat multiplier is wrong in both directions at once: still not enough headroom");
        System.out.println("for the workload it was designed to protect, and pure waste on the workload that never");
        System.out.println("had the tail risk the multiplier assumes every workload has.");
    }
}
