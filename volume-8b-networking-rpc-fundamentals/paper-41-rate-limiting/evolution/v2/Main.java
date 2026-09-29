import java.util.ArrayList;
import java.util.List;

/**
 * Replays the exact same request timeline as evolution/v1 -- 5 requests
 * clustered just before t=1000ms, 5 more clustered just after it -- against
 * the sliding-window log limiter, to show the boundary burst is gone.
 */
public class Main {
    public static void main(String[] args) {
        SlidingWindowLogLimiter limiter = new SlidingWindowLogLimiter(5, 1000);

        long[] beforeBoundary = {900, 920, 940, 960, 980};
        long[] afterBoundary = {1010, 1020, 1040, 1060, 1080};

        List<Long> allowedTimes = new ArrayList<>();

        System.out.println("Same 5 requests just before the old fixed-window boundary:");
        for (long t : beforeBoundary) {
            boolean allowed = limiter.allow(t);
            System.out.println("  t=" + t + "ms -> " + (allowed ? "ALLOWED" : "REJECTED"));
            if (allowed) allowedTimes.add(t);
        }

        System.out.println("Same 5 requests just after the old fixed-window boundary:");
        for (long t : afterBoundary) {
            boolean allowed = limiter.allow(t);
            System.out.println("  t=" + t + "ms -> " + (allowed ? "ALLOWED" : "REJECTED"));
            if (allowed) allowedTimes.add(t);
        }

        System.out.println();
        System.out.println("Total allowed: " + allowedTimes.size()
                + " requests, against a configured limit of 5 requests per trailing 1000ms.");
        System.out.println("No boundary burst: the second batch is rejected because the"
                + " trailing 1000ms window at t=1010..1080 still contains the first 5"
                + " requests (900..980), which haven't aged out yet.");
    }
}
