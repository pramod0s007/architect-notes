class OutboxEntry {
    final String eventType;
    final String orderId;
    boolean published = false;

    OutboxEntry(String eventType, String orderId) {
        this.eventType = eventType;
        this.orderId = orderId;
    }
}
