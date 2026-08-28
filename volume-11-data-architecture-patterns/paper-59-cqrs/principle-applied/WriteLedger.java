// Owns the write path only, and its own lock. Nothing that reads statements
// ever competes for this lock, no matter how expensive rendering a
// statement is.
class WriteLedger {
    private final Object lock = new Object();
    private long balanceCents = 0;
    private final ReadLedger readLedger;

    WriteLedger(ReadLedger readLedger) {
        this.readLedger = readLedger;
    }

    void transact(long amountCents) {
        synchronized (lock) {
            balanceCents += amountCents;
        }
        readLedger.apply(amountCents); // updates the read side, under ITS OWN lock
    }
}
