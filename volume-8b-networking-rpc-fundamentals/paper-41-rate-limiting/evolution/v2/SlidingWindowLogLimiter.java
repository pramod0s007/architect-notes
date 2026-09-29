import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Rate limiter that keeps a log of the actual timestamps of allowed requests
 * and only counts requests inside a continuously sliding window
 * [nowMs - windowSizeMs, nowMs] -- not a fixed, resetting window.
 *
 * This fixes the boundary-burst problem: there is no "reset to zero" moment,
 * so a request is only allowed if fewer than maxRequests happened in the
 * *trailing* windowSizeMs, no matter where that window falls on the clock.
 */
public class SlidingWindowLogLimiter {
    private final int maxRequestsPerWindow;
    private final long windowSizeMs;
    private final Deque<Long> requestLog = new ArrayDeque<>();

    public SlidingWindowLogLimiter(int maxRequestsPerWindow, long windowSizeMs) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowSizeMs = windowSizeMs;
    }

    /** Returns true if the request at time nowMs is allowed. */
    public boolean allow(long nowMs) {
        long windowStart = nowMs - windowSizeMs;
        // Evict log entries that have fallen out of the trailing window.
        while (!requestLog.isEmpty() && requestLog.peekFirst() <= windowStart) {
            requestLog.pollFirst();
        }
        if (requestLog.size() < maxRequestsPerWindow) {
            requestLog.addLast(nowMs);
            return true;
        }
        return false;
    }
}
