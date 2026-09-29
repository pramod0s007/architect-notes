import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the fixed-window boundary burst: a limiter capped at
 * "5 requests per 1000ms window" lets through 10 requests inside a single
 * 200ms span, because 5 of them land just before a window boundary and the
 * other 5 land just after it.
 */
public class Main {
    public static void main(String[] args) {
        FixedWindowLimiter limiter = new FixedWindowLimiter(5, 1000);

        // 5 requests clustered just BEFORE the t=1000 window boundary.
        long[] beforeBoundary = {900, 920, 940, 960, 980};
        // 5 requests clustered just AFTER the t=1000 window boundary.
        long[] afterBoundary = {1010, 1020, 1040, 1060, 1080};

        List<Long> allowedTimes = new ArrayList<>();

        System.out.println("Requests just before the window boundary (window [0,1000)):");
        for (long t : beforeBoundary) {
            boolean allowed = limiter.allow(t);
            System.out.println("  t=" + t + "ms -> " + (allowed ? "ALLOWED" : "REJECTED"));
            if (allowed) allowedTimes.add(t);
        }

        System.out.println("Requests just after the window boundary (window [1000,2000)):");
        for (long t : afterBoundary) {
            boolean allowed = limiter.allow(t);
            System.out.println("  t=" + t + "ms -> " + (allowed ? "ALLOWED" : "REJECTED"));
            if (allowed) allowedTimes.add(t);
        }

        long spanMs = allowedTimes.get(allowedTimes.size() - 1) - allowedTimes.get(0);
        System.out.println();
        System.out.println("Total allowed: " + allowedTimes.size() + " requests in a " + spanMs
                + "ms span, against a configured limit of 5 requests per 1000ms.");
        System.out.println("That's " + allowedTimes.size() + " requests in " + spanMs
                + "ms -- roughly 2x the intended rate, because the counter resets"
                + " to zero at the window boundary with no memory of the previous window.");
    }
}
