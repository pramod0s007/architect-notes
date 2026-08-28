// Applies whatever receipt update arrives, in arrival order. A DELIVERED
// receipt that got delayed in transit and arrives after READ overwrites it --
// the UI would show a message going from "Read" back to "Delivered."
class NaiveReceiptTracker {
    private ReceiptStatus status = ReceiptStatus.SENT;

    void update(ReceiptStatus newStatus) {
        status = newStatus;
    }

    ReceiptStatus status() {
        return status;
    }
}
