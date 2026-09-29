import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simulates a client paginating with the composite (createdAt, id) cursor
 * across the exact same tied-timestamp feed that broke the single-field
 * cursor in the wrong-direction scenario.
 */
public class Main {
    public static void main(String[] args) {
        EventStore store = new EventStore();
        int[] ids = {1, 2, 3, 4, 5, 6, 7, 8};
        long[] createdAt = {101, 102, 103, 104, 105, 105, 106, 107};
        for (int i = 0; i < ids.length; i++) {
            store.insert(new Event(ids[i], createdAt[i]));
        }

        Set<Integer> seen = new HashSet<>();
        Long cursorCreatedAt = null;
        Integer cursorId = null;
        int pageNum = 1;
        while (true) {
            EventStore.Page page = store.getPage(cursorCreatedAt, cursorId, 3);
            if (page.events.isEmpty()) {
                break;
            }
            System.out.println("=== Page " + pageNum + " (cursor=" + describeCursor(cursorCreatedAt, cursorId) + ") ===");
            printPage(page.events);
            for (var e : page.events) {
                boolean added = seen.add(e.id);
                if (!added) {
                    System.out.println("CONFIRMED FAILURE: event id=" + e.id + " was returned twice.");
                }
            }
            cursorCreatedAt = page.nextCursorCreatedAt;
            cursorId = page.nextCursorId;
            pageNum++;
        }

        System.out.println();
        System.out.println("Total events in store: " + ids.length);
        System.out.println("Total events returned across all pages: " + seen.size());
        boolean allPresent = true;
        for (int id : ids) {
            if (!seen.contains(id)) {
                allPresent = false;
            }
        }
        if (allPresent && seen.size() == ids.length) {
            System.out.println("FIXED: every event, including both tied-timestamp events (id=5 and id=6), "
                    + "was returned exactly once across the full traversal -- the (createdAt, id) composite "
                    + "cursor has no ties to break unpredictably, because id alone is already unique.");
        }
    }

    private static String describeCursor(Long createdAt, Integer id) {
        return createdAt == null ? "null" : "(" + createdAt + ", " + id + ")";
    }

    private static void printPage(List<Event> page) {
        StringBuilder sb = new StringBuilder("ids: [");
        for (int i = 0; i < page.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(page.get(i).id).append("(").append(page.get(i).createdAt).append(")");
        }
        sb.append("]");
        System.out.println(sb);
    }
}
