import java.util.HashMap;
import java.util.Map;

/**
 * A deduplication store bounded by a fixed retention window (in offsets), since
 * remembering every message id forever isn't realistic for a long-running consumer.
 * Configured here with a window that's smaller than the broker's actual guaranteed
 * maximum redelivery lag.
 */
public class DedupStore {
    private final int retentionWindow;
    private final Map<Integer, Integer> lastSeenAtOffset = new HashMap<>();

    public DedupStore(int retentionWindow) {
        this.retentionWindow = retentionWindow;
    }

    public boolean isDuplicate(int messageId) {
        return lastSeenAtOffset.containsKey(messageId);
    }

    public void recordSeen(int messageId, int currentOffset) {
        lastSeenAtOffset.put(messageId, currentOffset);
    }

    public void evictStaleRecords(int currentOffset) {
        lastSeenAtOffset.entrySet().removeIf(e -> currentOffset - e.getValue() > retentionWindow);
    }
}
