import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory sliding-window rate limiter keyed on client IP.
 *
 * <p>Tracks request counts in a fixed window.  A production implementation
 * would use Redis with TTL keys.
 */
public final class RateLimitHandler extends SecurityHandler {

    private final int maxRequestsPerWindow;
    private final Map<String, Integer> requestCounts = new HashMap<>();

    public RateLimitHandler(int maxRequestsPerWindow) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        String ip    = request.getIp();
        int    count = requestCounts.merge(ip, 1, Integer::sum);

        if (count > maxRequestsPerWindow) {
            System.out.printf("  [RateLimit]      BLOCKED  ip=%s count=%d/%d%n",
                    ip, count, maxRequestsPerWindow);
            return ApiResponse.tooManyRequests();
        }

        System.out.printf("  [RateLimit]      PASS     ip=%s count=%d/%d%n",
                ip, count, maxRequestsPerWindow);
        return passToNext(request);
    }

    /** Reset counts (useful between test scenarios). */
    public void reset() {
        requestCounts.clear();
    }
}
