// Resets to zero at fixed clock boundaries (every windowMs). Simple, and
// wrong at the boundary: nothing stops a client from spending its whole
// budget in the last millisecond of one window and the whole budget again
// in the first millisecond of the next.
class FixedWindowCounter {
    private final int limit;
    private final long windowMs;
    private long windowStart = -1;
    private int count;

    FixedWindowCounter(int limit, long windowMs) {
        this.limit = limit;
        this.windowMs = windowMs;
    }

    boolean allow(long now) {
        long thisWindowStart = (now / windowMs) * windowMs;
        if (thisWindowStart != windowStart) {
            windowStart = thisWindowStart;
            count = 0;
        }
        if (count < limit) {
            count++;
            return true;
        }
        return false;
    }
}
