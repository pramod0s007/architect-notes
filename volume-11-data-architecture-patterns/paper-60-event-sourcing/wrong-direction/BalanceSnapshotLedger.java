import java.util.ArrayList;
import java.util.List;

// Looks like event sourcing -- it stores something after every transaction,
// not just the current balance. What it stores is the RESULTING BALANCE,
// not the event that produced it. The transaction type is accepted as a
// parameter and immediately discarded.
class BalanceSnapshotLedger {
    private final List<Long> balanceSnapshots = new ArrayList<>();
    private long balanceCents = 0;

    void transact(TransactionType type, long amountCents) {
        balanceCents += amountCents; // 'type' was never stored anywhere
        balanceSnapshots.add(balanceCents);
    }

    long balance() {
        return balanceCents;
    }

    List<Long> snapshots() {
        return balanceSnapshots;
    }
}
