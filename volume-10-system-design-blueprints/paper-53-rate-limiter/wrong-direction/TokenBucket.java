// Refills continuously instead of resetting at a boundary. A burst can only
// spend tokens that have actually accumulated -- there's no clock edge where
// the budget silently doubles.
class TokenBucket {
    private final int capacity;
    private final double refillPerMs;
    private double tokens;
    private long lastRefillTime;

    TokenBucket(int capacity, double refillPerMs, long now) {
        this.capacity = capacity;
        this.refillPerMs = refillPerMs;
        this.tokens = capacity;
        this.lastRefillTime = now;
    }

    boolean allow(long now) {
        refill(now);
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    private void refill(long now) {
        long elapsed = now - lastRefillTime;
        if (elapsed > 0) {
            tokens = Math.min(capacity, tokens + elapsed * refillPerMs);
            lastRefillTime = now;
        }
    }
}
