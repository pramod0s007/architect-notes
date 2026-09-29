/**
 * PRESSURE: the broker guarantees it will never redeliver a message more than 3 offsets
 * after it was first delivered. To keep the dedup store's memory bounded, this consumer
 * evicts records after a retention window of just 1 offset -- smaller than the broker's
 * actual guarantee. A legitimate, in-guarantee redelivery of message 100 (arriving 3
 * offsets after its original delivery) shows up AFTER its dedup record was already
 * evicted, and is falsely treated as brand new.
 */
public class Main {
    private static final int RETENTION_WINDOW = 1; // too small: less than the broker's real guarantee of 3

    public static void main(String[] args) {
        DedupStore store = new DedupStore(RETENTION_WINDOW);
        int processedCount = 0;

        processedCount += process(store, 100, 0); // first delivery
        processedCount += process(store, 200, 1); // different message
        processedCount += process(store, 300, 2); // different message
        processedCount += process(store, 100, 3); // legitimate redelivery, within the broker's real 3-offset guarantee

        System.out.println();
        System.out.println("Total messages actually processed: " + processedCount + " (expected 3 distinct messages)");
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
