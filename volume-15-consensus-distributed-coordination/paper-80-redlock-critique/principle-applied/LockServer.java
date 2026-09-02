// One of several independent lock servers a Redlock-style client acquires a
// majority of. clockSpeedFactor simulates real clock drift between machines --
// 1.0 is a perfectly accurate clock, 1.4 runs 40% fast. Real hardware clocks
// drift by far less than this per lock attempt, but by an amount that is
// neither zero nor bounded to what any protocol can assume away entirely --
// this number is exaggerated here to make the mechanism visible, not to claim
// real clocks drift this much.
class LockServer {
    final String id;
    private final double clockSpeedFactor;
    private boolean locked = false;
    private double lockAcquiredAtRealTime;
    private double ttlMs;

    LockServer(String id, double clockSpeedFactor) {
        this.id = id;
        this.clockSpeedFactor = clockSpeedFactor;
    }

    boolean tryAcquire(double currentRealTime, double ttlMs) {
        if (locked && !isExpiredAt(currentRealTime)) {
            return false;
        }
        locked = true;
        lockAcquiredAtRealTime = currentRealTime;
        this.ttlMs = ttlMs;
        return true;
    }

    private boolean isExpiredAt(double currentRealTime) {
        double elapsedOwnClockTime = (currentRealTime - lockAcquiredAtRealTime) * clockSpeedFactor;
        return elapsedOwnClockTime >= ttlMs;
    }
}
