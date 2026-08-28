// Fires when the error rate in a short recent window crosses a fixed,
// high threshold -- tuned to catch dramatic outages without paging on
// every minor blip.
class NaiveThresholdAlerter {
    private static final double THRESHOLD = 0.10; // 10% error rate

    boolean shouldAlert(int requestsInWindow, int errorsInWindow) {
        double rate = (double) errorsInWindow / requestsInWindow;
        return rate > THRESHOLD;
    }
}
