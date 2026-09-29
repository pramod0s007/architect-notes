/**
 * Rate limiter that counts requests in fixed, non-overlapping clock windows
 * (e.g. [0,1000), [1000,2000), ...). Resets the counter to zero the instant
 * a new window starts.
 *
 * The clock is passed in explicitly (not System.currentTimeMillis()) so the
 * boundary-burst behavior below is exactly reproducible.
 */
public class FixedWindowLimiter {
    private final int maxRequestsPerWindow;
    private final long windowSizeMs;

    private long currentWindowStart = -1;
    private int countInWindow = 0;

    public FixedWindowLimiter(int maxRequestsPerWindow, long windowSizeMs) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowSizeMs = windowSizeMs;
    }

    /** Returns true if the request at time nowMs is allowed. */
    public boolean allow(long nowMs) {
        long windowStart = (nowMs / windowSizeMs) * windowSizeMs;
        if (windowStart != currentWindowStart) {
            // New window: counter resets to zero, no memory of the previous window.
            currentWindowStart = windowStart;
            countInWindow = 0;
        }
        if (countInWindow < maxRequestsPerWindow) {
            countInWindow++;
            return true;
        }
        return false;
    }
}
