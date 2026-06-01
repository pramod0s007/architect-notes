import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logs every price change to an in-memory audit trail with a timestamp.
 *
 * <p>A production implementation would write to a persistent store
 * (database, append-only log file, event bus).
 */
public final class AuditLogObserver implements StockPriceObserver {

    private final List<String> log = new ArrayList<>();

    @Override
    public void onPriceChanged(StockPriceEvent event) {
        String entry = String.format("[%s] %s",
                event.getTimestamp(), event);
        log.add(entry);
        System.out.println("  [AuditLog]  " + entry);
    }

    /** Returns an unmodifiable view of all audit entries recorded so far. */
    public List<String> getLog() {
        return Collections.unmodifiableList(log);
    }
}
