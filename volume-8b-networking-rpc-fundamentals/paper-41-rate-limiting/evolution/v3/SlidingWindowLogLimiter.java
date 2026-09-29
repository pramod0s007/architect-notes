import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Same sliding-window-log algorithm as evolution/v2's SlidingWindowLogLimiter,
 * reused unchanged. Kept as a separate class name here only to make the two
 * "one limiter per app instance" vs "one shared limiter" scenarios in Main
 * easy to tell apart in the output; the logic is identical to v2.
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
