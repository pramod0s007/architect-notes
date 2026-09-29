/**
 * One backend app instance behind a load balancer. Each instance owns its
 * own in-process SlidingWindowLogLimiter, so it can only see and count the
 * requests that were routed to *it* -- it has no visibility into what the
 * other instances are allowing.
 */
public class AppInstance {
    private final String name;
    private final SlidingWindowLogLimiter localLimiter;

    public AppInstance(String name, int maxRequestsPerWindow, long windowSizeMs) {
        this.name = name;
        this.localLimiter = new SlidingWindowLogLimiter(maxRequestsPerWindow, windowSizeMs);
    }

    public String getName() {
        return name;
    }

    public boolean handleRequest(long nowMs) {
        return localLimiter.allow(nowMs);
    }
}
