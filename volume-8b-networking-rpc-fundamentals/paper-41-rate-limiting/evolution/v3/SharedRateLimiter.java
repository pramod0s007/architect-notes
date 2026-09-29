/**
 * A centralized rate limiter shared by every app instance -- standing in for
 * a real deployment's shared store (e.g. a Redis counter) that all instances
 * call out to instead of keeping their own local count.
 *
 * Because there is exactly one SlidingWindowLogLimiter backing every
 * instance's calls, the global cap is enforced no matter how many instances
 * are running or how requests are distributed across them.
 */
public class SharedRateLimiter {
    private final SlidingWindowLogLimiter sharedLimiter;

    public SharedRateLimiter(int maxRequestsPerWindow, long windowSizeMs) {
        this.sharedLimiter = new SlidingWindowLogLimiter(maxRequestsPerWindow, windowSizeMs);
    }

    /** Called by every app instance in place of a local limiter. */
    public synchronized boolean allow(long nowMs) {
        return sharedLimiter.allow(nowMs);
    }
}
