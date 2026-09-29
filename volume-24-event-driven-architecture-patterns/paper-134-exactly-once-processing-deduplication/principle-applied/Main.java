/**
 * PRINCIPLE APPLIED: set the dedup store's retention window to actually match the
 * broker's real guaranteed maximum redelivery lag (3 offsets), instead of guessing too
 * small (pressure) or refusing to evict at all (wrong direction). A legitimate
 * redelivery within that guarantee is still caught as a duplicate. An id reused only
 * after that guarantee has genuinely expired is correctly treated as a new message --
 * because at that point, it safely could no longer be a real redelivery of the original.
 */
public class Main {
    private static final int RETENTION_WINDOW = 3; // matches the broker's actual guaranteed max redelivery lag

    public static void main(String[] args) {
        DedupStore store = new DedupStore(RETENTION_WINDOW);
        int processedCount = 0;

        processedCount += process(store, 100, 0);  // first delivery
        processedCount += process(store, 200, 1);  // different message
        processedCount += process(store, 300, 2);  // different message
        processedCount += process(store, 100, 3);  // legitimate redelivery -- correctly caught

        // Much later, id 100 has genuinely been recycled for a brand-new, unrelated message,
        // well outside the broker's guaranteed redelivery window
        processedCount += process(store, 100, 50);

        System.out.println();
        System.out.println("Total messages actually processed: " + processedCount
                + " (expected 4: 3 originals + 1 genuinely new message)");
    }

    private static int process(DedupStore store, int messageId, int offset) {
        store.evictStaleRecords(offset);
        if (store.isDuplicate(messageId)) {
            System.out.println("Offset " + offset + ": message " + messageId + " is a duplicate -- skipped");
            return 0;
        }
        store.recordSeen(messageId, offset);
        System.out.println("Offset " + offset + ": message " + messageId + " processed");
        return 1;
    }
}
