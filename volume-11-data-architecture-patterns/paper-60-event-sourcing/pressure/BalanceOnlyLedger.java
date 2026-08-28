// Keeps only the current balance. Every transaction updates it in place --
// once applied, the individual transaction that caused the change is gone.
// The current total is all that survives.
class BalanceOnlyLedger {
    private long balanceCents = 0;

    void transact(long amountCents) {
        balanceCents += amountCents;
    }

    long balance() {
        return balanceCents;
    }
}
