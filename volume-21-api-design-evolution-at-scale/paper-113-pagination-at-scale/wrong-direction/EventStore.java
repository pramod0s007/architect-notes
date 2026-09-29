import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The team's fix after the offset-pagination incident: "switch to cursor
 * pagination -- instead of an offset, remember the last row's createdAt
 * and ask for everything strictly older than it." This correctly stops
 * new inserts from shifting anyone's position. But the cursor is a single
 * field, and that field is not guaranteed unique: when two events share
 * the exact same createdAt, "strictly older than the cursor" silently
 * excludes any tied sibling of the last row returned on a page, because
 * that sibling is not older than the cursor -- it's equal to it.
 */
public class EventStore {
    private final List<Event> events = new ArrayList<>();

    public void insert(Event e) {
        events.add(e);
    }

    /**
     * Returns the next page strictly older than "cursorCreatedAt" (null means
     * "start from the newest"), sorted newest-first, stable on ties.
     */
    public Page getPage(Long cursorCreatedAt, int limit) {
        List<Event> sorted = new ArrayList<>(events);
        sorted.sort(Comparator.comparingLong((Event e) -> e.createdAt).reversed());

        List<Event> page = new ArrayList<>();
        for (Event e : sorted) {
            if (cursorCreatedAt != null && e.createdAt >= cursorCreatedAt) {
                continue; // MISTAKE: excludes ties, not just rows already returned
            }
            page.add(e);
            if (page.size() == limit) {
                break;
            }
        }
        Long nextCursor = page.isEmpty() ? cursorCreatedAt : page.get(page.size() - 1).createdAt;
        return new Page(page, nextCursor);
    }

    public static class Page {
        public final List<Event> events;
        public final Long nextCursor;

        public Page(List<Event> events, Long nextCursor) {
            this.events = events;
            this.nextCursor = nextCursor;
        }
    }
}
