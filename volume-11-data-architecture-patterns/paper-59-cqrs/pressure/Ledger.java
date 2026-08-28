import java.util.ArrayList;
import java.util.List;

// One model, one lock, serving both transactions and statements. A
// statement has to scan the full transaction history to build itself --
// and while it holds the lock to do that, no transaction can be recorded.
class Ledger {
    private final List<Transaction> transactions = new ArrayList<>();
    private long balanceCents = 0;

    synchronized void transact(long amountCents) {
        transactions.add(new Transaction(amountCents));
        balanceCents += amountCents;
    }

    synchronized long statement() {
        long total = 0;
        for (Transaction t : transactions) {
            total += t.amountCents; // stands in for real statement-rendering work
            simulateRenderingCost();
        }
        return total;
    }

    private void simulateRenderingCost() {
        try {
            Thread.sleep(1); // a realistic stand-in for per-line statement formatting
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
