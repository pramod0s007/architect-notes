// Wrong direction: marks the entry published optimistically, without checking
// whether the broker actually confirmed it. Looks identical to the real relay
// at a glance -- the bug is only in the order of two lines.
class OutboxRelayBroken {
    private final Outbox outbox;
    private final FlakyMessageBroker broker;

    OutboxRelayBroken(Outbox outbox, FlakyMessageBroker broker) {
        this.outbox = outbox;
        this.broker = broker;
    }

    void pollOnce() {
        for (OutboxEntry entry : outbox.unpublished()) {
            entry.published = true; // marked done BEFORE knowing whether it actually is
            broker.publish(entry.eventType, entry.orderId);
        }
    }
}
