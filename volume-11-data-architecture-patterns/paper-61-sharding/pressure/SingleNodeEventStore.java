import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Every account's events live in one store, behind one lock. Two completely
// unrelated accounts' appends still compete for the same lock, because
// there's only one node handling everyone.
class SingleNodeEventStore {
    private final Object lock = new Object();
    private final Map<String, List<Long>> accountEvents = new HashMap<>();

    void append(String accountId, long amountCents) {
        synchronized (lock) {
            accountEvents.computeIfAbsent(accountId, k -> new ArrayList<>()).add(amountCents);
            simulateWork();
        }
    }

    private void simulateWork() {
        try {
            Thread.sleep(1); // a realistic stand-in for a real append's I/O cost
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
