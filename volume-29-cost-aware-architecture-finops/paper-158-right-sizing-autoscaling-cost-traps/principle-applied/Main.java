import java.util.*;

public class Main {

    static final double PRICE_PER_VCPU_MONTH = 30.0; // $/vCPU/month, illustrative flat rate
    static final double SAFETY_MARGIN = 0.10;         // 10% margin over each workload's own observed peak
    static final int MIN_SAMPLES_TO_TRUST_PEAK = 14;  // ~2 weeks, enough to see a weekly cycle at least once

    static double monthlyCost(double requestMillicores, int replicas) {
        return (requestMillicores / 1000.0) * PRICE_PER_VCPU_MONTH * replicas;
    }

    static double max(double[] usage) {
        double m = usage[0];
        for (double u : usage) if (u > m) m = u;
        return m;
    }

    static class Workload {
        String name;
        double[] usage;
        int replicas;
        double fallbackRequest; // this workload's prior known-safe request (e.g. a pre-launch load-test
                                 // baseline for a brand-new workload), used only when there isn't enough
                                 // history to trust a peak-based size computed from this workload's own data

        Workload(String name, double[] usage, int replicas, double fallbackRequest) {
            this.name = name;
            this.usage = usage;
            this.replicas = replicas;
            this.fallbackRequest = fallbackRequest;
        }
    }

    /**
     * Sizes a CPU request from a workload's own observed daily-peak usage history: its own highest
     * observed daily peak plus a disclosed safety margin, not a fleet-wide average or a flat multiplier
     * borrowed from another workload's risk profile. If there isn't enough history to trust that peak,
     * falls back to this workload's own prior known-safe request instead of sizing off a statistically
     * thin sample. (This model does not distinguish a repeatable peak from a one-off anomaly in the
     * history -- see the Cons section for that limitation.)
     */
    static double sizeRequest(Workload w) {
        if (w.usage.length < MIN_SAMPLES_TO_TRUST_PEAK) {
            System.out.printf("%s: only %d day(s) of history (need >= %d) -- INSUFFICIENT_HISTORY,%n",
                    w.name, w.usage.length, MIN_SAMPLES_TO_TRUST_PEAK);
            System.out.printf("  using conservative fallback request=%.0fm, flagged for re-evaluation once more history accumulates%n",
                    w.fallbackRequest);
            return w.fallbackRequest;
        }
        double peak = max(w.usage);
        // Sample data is in whole millicores; the -1e-9 epsilon guard exists only to absorb the binary
        // floating-point error introduced by (peak * 1.10), so an exact multiple-of-10 target (e.g. 990.0)
        // isn't pushed to the next 10m by a residual like 990.0000000000001. It is not a general-purpose
        // rounding guarantee for arbitrary non-integer input.
        double sized = Math.ceil((peak * (1 + SAFETY_MARGIN) / 10.0) - 1e-9) * 10.0; // round UP to nearest 10m
        System.out.printf("%s: %d days of history, own peak=%.0fm -> request=%.0fm (peak + %.0f%% margin)%n",
                w.name, w.usage.length, peak, sized, SAFETY_MARGIN * 100);
        return sized;
    }

    public static void main(String[] args) {
        // Each sample is that day's own peak per-replica CPU demand (whole millicores), not a daily average.
        double[] checkoutUsage = {
            210, 225, 198, 240, 205, 215, 230, 200, 220, 235,
            900, 210, 225, 198, 240, 205, 215, 230, 200, 220,
            235, 210, 900, 225, 198, 240, 205, 215, 230, 220
        };
        double[] searchUsage = {
            200, 202, 198, 205, 199, 203, 201, 204, 198, 206,
            200, 202, 199, 205, 201, 203, 198, 206, 200, 202,
            199, 205, 201, 203, 198, 206, 200, 202, 199, 205
        };
        // A workload that launched 3 days ago -- far too little history to trust a peak-based size,
        // regardless of what those 3 samples happen to show.
        double[] recommendationsV2Usage = {80, 95, 110};

        // The 1000.0/500.0 fallback values below are unused for checkout/search in this run (both clear
        // the 14-sample threshold on their own history) but represent each workload's own prior known-safe
        // request. For recommendations-v2, 500.0 represents its pre-launch load-test-derived baseline
        // request, established before the workload had any production history of its own.
        Workload checkout = new Workload("checkout", checkoutUsage, 4, 1000.0);
        Workload search = new Workload("search", searchUsage, 6, 500.0);
        Workload recommendationsV2 = new Workload("recommendations-v2", recommendationsV2Usage, 2, 500.0);

        System.out.println("-- Sizing each workload's CPU request from its own observed usage history --");
        double checkoutRequest = sizeRequest(checkout);
        double searchRequest = sizeRequest(search);
        double recommendationsV2Request = sizeRequest(recommendationsV2);

        double checkoutCost = monthlyCost(checkoutRequest, checkout.replicas);
        double searchCost = monthlyCost(searchRequest, search.replicas);
        double recommendationsV2Cost = monthlyCost(recommendationsV2Request, recommendationsV2.replicas);

        System.out.println();
        System.out.println("-- Resulting monthly cost per workload --");
        System.out.printf("checkout:            request=%.0fm x %d replicas -> $%.2f/month%n",
                checkoutRequest, checkout.replicas, checkoutCost);
        System.out.printf("search:              request=%.0fm x %d replicas -> $%.2f/month%n",
                searchRequest, search.replicas, searchCost);
        System.out.printf("recommendations-v2:  request=%.0fm x %d replicas -> $%.2f/month%n",
                recommendationsV2Request, recommendationsV2.replicas, recommendationsV2Cost);

        // Replay the same flash-sale day against checkout's new, individually-sized capacity.
        double spikeDemand = max(checkoutUsage);
        System.out.println();
        System.out.println("-- Next flash-sale day arrives, demand repeats the historical spike --");
        System.out.printf("checkout: demand=%.0fm, capacity=%.0fm%n", spikeDemand, checkoutRequest);
        if (spikeDemand > checkoutRequest) {
            System.out.println("THROTTLED: capacity still short of demand.");
        } else {
            System.out.println("OK: capacity covers demand, sized from this workload's own observed peak.");
        }

        System.out.println();
        System.out.println("-- Comparing this policy's checkout and search costs against the blanket 2.5x-average policy --");
        double blanketCheckoutRequest = 660.0; // from the wrong-direction scenario
        double blanketSearchRequest = 500.0;   // from the wrong-direction scenario
        System.out.printf("checkout: this policy=%.0fm ($%.2f/mo) vs blanket=%.0fm ($%.2f/mo) -- covers the spike where blanket still didn't%n",
                checkoutRequest, checkoutCost, blanketCheckoutRequest, monthlyCost(blanketCheckoutRequest, checkout.replicas));
        System.out.printf("search:   this policy=%.0fm ($%.2f/mo) vs blanket=%.0fm ($%.2f/mo) -- saves $%.2f/month on a workload with no observed spike in this history%n",
                searchRequest, searchCost, blanketSearchRequest, monthlyCost(blanketSearchRequest, search.replicas),
                monthlyCost(blanketSearchRequest, search.replicas) - searchCost);

        System.out.println();
        System.out.println("-- Note on cost --");
        System.out.println("Sizing checkout to its own peak costs nearly as much as the original, un-rightsized request --");
        System.out.println("that's not a failure of this policy, it's what a request sized to this workload's own observed peak,");
        System.out.println("under this disclosed margin, costs for a genuinely spiky workload. The savings this policy delivers");
        System.out.println("come from sizing every OTHER, lower-variance workload to its own observed peak instead, rather than");
        System.out.println("paying checkout's risk premium on workloads whose own history shows no reason to need it.");
    }
}
