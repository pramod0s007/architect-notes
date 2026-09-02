// One purchase. eventTime is when the purchase actually happened (embedded in
// the event by the source system). arrivalTime is when this stream processor
// actually received it -- always >= eventTime, and can lag behind it by an
// unpredictable amount due to network delay, retries, or client buffering.
class PurchaseEvent {
    final int eventTime;
    final int arrivalTime;
    final int amountCents;

    PurchaseEvent(int eventTime, int arrivalTime, int amountCents) {
        this.eventTime = eventTime;
        this.arrivalTime = arrivalTime;
        this.amountCents = amountCents;
    }
}
