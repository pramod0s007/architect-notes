// Only moves forward through SENT -> DELIVERED -> READ. A receipt that
// arrives late and represents an earlier state than the one already shown
// is simply ignored -- status can never regress.
class MonotonicReceiptTracker {
    private ReceiptStatus status = ReceiptStatus.SENT;

    void update(ReceiptStatus newStatus) {
        if (newStatus.ordinal() > status.ordinal()) {
            status = newStatus;
        }
    }

    ReceiptStatus status() {
        return status;
    }
}
