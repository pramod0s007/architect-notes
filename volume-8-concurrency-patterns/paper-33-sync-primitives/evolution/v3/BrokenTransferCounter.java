// Each field is individually "thread-safe" — but the invariant that spans both
// fields (inFlight + completed == totalStarted) is not, because a reader can
// observe the gap between the two separate synchronized blocks.
class BrokenTransferCounter {
    private final Object inFlightLock = new Object();
    private final Object completedLock = new Object();
    private int inFlight = 0;
    private int completed = 0;

    void requestStarted() {
        synchronized (inFlightLock) {
            inFlight++;
        }
    }

    void requestFinished() {
        synchronized (inFlightLock) {
            inFlight--;
        }
        // <-- window: a reader can see inFlight already decremented but completed not yet incremented
        synchronized (completedLock) {
            completed++;
        }
    }

    int getInFlight() {
        synchronized (inFlightLock) {
            return inFlight;
        }
    }

    int getCompleted() {
        synchronized (completedLock) {
            return completed;
        }
    }
}
