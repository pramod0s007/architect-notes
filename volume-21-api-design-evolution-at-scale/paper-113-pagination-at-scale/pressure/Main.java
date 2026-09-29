import java.util.List;

/**
 * Simulates a client paginating a live, newest-first feed with OFFSET/LIMIT
 * while a new event is inserted between page requests -- exactly what
 * happens on any real feed between two page loads.
 */
public class Main {
    public static void main(String[] args) {
        EventStore store = new EventStore();
        for (int id = 1; id <= 7; id++) {
            store.insert(new Event(id, 100 + id)); // ids 1..7, createdAt 101..107, ascending (7 is newest)
        }

        System.out.println("=== Page 1: offset=0, limit=3 (before any new insert) ===");
        List<Event> page1 = store.getPage(0, 3);
        printPage(page1);

        System.out.println();
        System.out.println("=== A new event (id=8) arrives, newer than everything so far ===");
        store.insert(new Event(8, 108));

        System.out.println();
        System.out.println("=== Page 2: offset=3, limit=3 (client just increments the offset it was given) ===");
        List<Event> page2 = store.getPage(3, 3);
        printPage(page2);

        System.out.println();
        boolean overlap = false;
        for (Event e1 : page1) {
            for (Event e2 : page2) {
                if (e1.id == e2.id) {
                    overlap = true;
                }
            }
        }
        if (overlap) {
            System.out.println("CONFIRMED FAILURE: page 1 and page 2 both contain events " + overlapIds(page1, page2)
                    + " -- the client rendered the same events twice, because the single new insert shifted every "
                    + "existing row's offset by one, and the client's \"offset\" was never a stable position, just a count.");
        }
    }

    private static void printPage(List<Event> page) {
        StringBuilder sb = new StringBuilder("ids: [");
        for (int i = 0; i < page.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(page.get(i).id);
        }
        sb.append("]");
        System.out.println(sb);
    }

    private static String overlapIds(List<Event> page1, List<Event> page2) {
        StringBuilder sb = new StringBuilder();
        for (Event e1 : page1) {
            for (Event e2 : page2) {
                if (e1.id == e2.id) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(e1.id);
                }
            }
        }
        return sb.toString();
    }
}
