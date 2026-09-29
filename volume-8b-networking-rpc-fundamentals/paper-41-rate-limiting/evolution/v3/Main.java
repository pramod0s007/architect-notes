import java.util.ArrayList;
import java.util.List;

/**
 * Scenario 1: three app instances behind a load balancer, each enforcing its
 * OWN local "5 requests per 1000ms" limit. 15 requests are spread round-robin
 * across the 3 instances, all within a single 1000ms window. Each instance
 * individually obeys its 5-per-window cap -- but the load balancer has
 * fanned the traffic out so well that the *global* system allows 15 requests
 * in that window, 3x the intended limit.
 *
 * Scenario 2: the same 15 requests are replayed through a single
 * SharedRateLimiter that all three instances call into instead of keeping
 * their own counters, correctly capping the total at 5.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Scenario 1: each instance has its own local limiter ===");
        AppInstance[] instances = {
                new AppInstance("instance-A", 5, 1000),
                new AppInstance("instance-B", 5, 1000),
                new AppInstance("instance-C", 5, 1000),
        };

        List<Boolean> localResults = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            long t = 100 + i * 10L;
            AppInstance instance = instances[i % 3];
            boolean allowed = instance.handleRequest(t);
            localResults.add(allowed);
            System.out.println("  t=" + t + "ms routed to " + instance.getName() + " -> "
                    + (allowed ? "ALLOWED" : "REJECTED"));
        }
        long localAllowedCount = localResults.stream().filter(Boolean::booleanValue).count();
        System.out.println("Total allowed globally: " + localAllowedCount
                + " requests in a single 1000ms window, against a configured GLOBAL limit of 5.");
        System.out.println("Each instance individually obeyed its own 5-per-window cap; the"
                + " global cap was blown by " + (localAllowedCount / 5) + "x because none of"
                + " the 3 instances know about the other 2.");

        System.out.println();
        System.out.println("=== Scenario 2: all instances share one centralized limiter ===");
        SharedRateLimiter sharedLimiter = new SharedRateLimiter(5, 1000);

        List<Boolean> sharedResults = new ArrayList<>();
        String[] instanceNames = {"instance-A", "instance-B", "instance-C"};
        for (int i = 0; i < 15; i++) {
            long t = 100 + i * 10L;
            String instanceName = instanceNames[i % 3];
            boolean allowed = sharedLimiter.allow(t);
            sharedResults.add(allowed);
            System.out.println("  t=" + t + "ms routed to " + instanceName + " -> "
                    + (allowed ? "ALLOWED" : "REJECTED"));
        }
        long sharedAllowedCount = sharedResults.stream().filter(Boolean::booleanValue).count();
        System.out.println("Total allowed globally: " + sharedAllowedCount
                + " requests in a single 1000ms window, against a configured GLOBAL limit of 5.");
        System.out.println("Correctly capped at 5, regardless of which instance each request"
                + " landed on, because every instance is checking the same counter.");
    }
}
