class OutboxRelay {
    private final Outbox outbox;
    private final FlakyMessageBroker broker;

    OutboxRelay(Outbox outbox, FlakyMessageBroker broker) {
        this.outbox = outbox;
        this.broker = broker;
    }

    // One poll: try to publish everything not yet published. Only mark an
    // entry published AFTER the broker confirms it -- a failed attempt
    // leaves the entry exactly where the next poll will find it again.
    void pollOnce() {
        for (OutboxEntry entry : outbox.unpublished()) {
            boolean ok = broker.publish(entry.eventType, entry.orderId);
            if (ok) {
                entry.published = true;
            }
        }
    }
}
