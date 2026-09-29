/**
 * WRONG DIRECTION: "fix" the too-small retention window by never evicting anything --
 * remember every message id forever, so a legitimate redelivery can never slip past the
 * dedup check. This does correctly catch message 100's redelivery at offset 3. But much
 * later, after id 100 has genuinely been recycled by the upstream system for a completely
 * different, unrelated message, the dedup store still has the original id-100 record and
 * has no way to tell "this is a redelivery" from "this id happens to be reused." The
 * brand-new message is wrongly treated as a duplicate and silently skipped -- never
 * processed at all.
 */
public class Main {
    public static void main(String[] args) {
        DedupStore store = new DedupStore(Integer.MAX_VALUE); // "fix": retain every record forever, never evict

        int processedCount = 0;

        processedCount += process(store, 100, 0);  // first delivery of a real message
        processedCount += process(store, 200, 1);   // different message
        processedCount += process(store, 300, 2);   // different message
        processedCount += process(store, 100, 3);   // legitimate redelivery -- correctly caught this time

        // Much later, id 100 has genuinely been recycled for a brand-new, unrelated message
        processedCount += process(store, 100, 50);

        System.out.println();
        System.out.println("Total messages actually processed: " + processedCount
                + " (expected 4: 3 originals + 1 genuinely new message reusing id 100)");
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
