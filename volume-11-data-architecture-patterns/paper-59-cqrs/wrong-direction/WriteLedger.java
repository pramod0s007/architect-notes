class WriteLedger {
    private final Object lock = new Object();
    private long balanceCents = 0;
    private final AsyncReadLedger readLedger;

    WriteLedger(AsyncReadLedger readLedger) {
        this.readLedger = readLedger;
    }

    void transact(long amountCents) {
        synchronized (lock) {
            balanceCents += amountCents;
        }
        readLedger.applyWithLag(amountCents, 200); // replication takes 200ms to land
    }

    long balance() {
        synchronized (lock) {
            return balanceCents;
        }
    }
}
