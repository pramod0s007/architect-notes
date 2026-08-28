// Looks like the same fix as MonotonicReceiptTracker -- reject updates that
// are "older" than the current one. "Older" here means "has a smaller
// client-supplied timestamp," and that timestamp comes from whichever
// device generated the receipt, not a shared, authoritative clock.
class TimestampReceiptTracker {
    private ReceiptStatus status = ReceiptStatus.SENT;
    private long lastUpdateTimestamp = Long.MIN_VALUE;

    void update(ReceiptStatus newStatus, long clientTimestamp) {
        if (clientTimestamp > lastUpdateTimestamp) {
            status = newStatus;
            lastUpdateTimestamp = clientTimestamp;
        }
    }

    ReceiptStatus status() {
        return status;
    }
}
