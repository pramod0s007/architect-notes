// One payment-charge event on the stream. messageId uniquely identifies THIS
// send attempt by the producer -- a redelivery of the same logical event
// carries the SAME messageId, which is exactly what a consumer needs to tell
// "redelivered" apart from "a new, distinct charge."
class Event {
    final String messageId;
    final String accountId;
    final int amountCents;

    Event(String messageId, String accountId, int amountCents) {
        this.messageId = messageId;
        this.accountId = accountId;
        this.amountCents = amountCents;
    }
}
