import java.util.ArrayList;
import java.util.List;

// The event log IS the source of truth. Current balance is a VIEW derived
// by replaying it -- never the only thing that survives. Any future
// question about what happened can be answered by replaying the same log
// a different way, without ever touching how transactions were recorded.
class EventSourcedLedger {
    private final List<TransactionEvent> events = new ArrayList<>();

    void transact(long amountCents) {
        events.add(new TransactionEvent(amountCents));
    }

    long balance() {
        long total = 0;
        for (TransactionEvent e : events) {
            total += e.amountCents;
        }
        return total;
    }

    List<TransactionEvent> largeTransactions(long thresholdCents) {
        List<TransactionEvent> result = new ArrayList<>();
        for (TransactionEvent e : events) {
            if (Math.abs(e.amountCents) > thresholdCents) {
                result.add(e);
            }
        }
        return result;
    }
}
