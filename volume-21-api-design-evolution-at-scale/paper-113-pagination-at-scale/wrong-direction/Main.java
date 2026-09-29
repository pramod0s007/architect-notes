import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simulates a client paginating with the "single-field cursor" fix across a
 * feed where two events share the exact same createdAt value.
 */
public class Main {
    public static void main(String[] args) {
        EventStore store = new EventStore();
        // ids 1..8, createdAt ascending EXCEPT id 5 and id 6 share the same value (105).
        int[] ids = {1, 2, 3, 4, 5, 6, 7, 8};
        long[] createdAt = {101, 102, 103, 104, 105, 105, 106, 107};
        for (int i = 0; i < ids.length; i++) {
            store.insert(new Event(ids[i], createdAt[i]));
        }

        Set<Integer> seen = new HashSet<>();
        Long cursor = null;
        int pageNum = 1;
        while (true) {
            EventStore.Page page = store.getPage(cursor, 3);
            if (page.events.isEmpty()) {
                break;
            }
            System.out.println("=== Page " + pageNum + " (cursor=" + cursor + ") ===");
            printPage(page.events);
            for (var e : page.events) {
                seen.add(e.id);
            }
            cursor = page.nextCursor;
            pageNum++;
        }

        System.out.println();
        System.out.println("Total events in store: " + ids.length);
        System.out.println("Total events returned across all pages: " + seen.size());
        for (int id : ids) {
            if (!seen.contains(id)) {
                System.out.println("CONFIRMED FAILURE: event id=" + id + " (createdAt=105, tied with id=5) "
                        + "was never returned on any page -- it was excluded forever because the page that "
                        + "ended on its tied sibling (id=5) set the cursor to exactly 105, and \"strictly older "
                        + "than 105\" silently drops id=6 as well, which is equal to 105, not older than it.");
            }
        }
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
