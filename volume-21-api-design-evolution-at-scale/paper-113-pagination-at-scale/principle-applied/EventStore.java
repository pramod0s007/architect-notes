import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The real fix: the cursor is a composite of (createdAt, id), not createdAt
 * alone. "id" is unique and monotonic, so the pair (createdAt, id) defines a
 * strict total order across every event -- no two events can ever tie on
 * both fields at once, so "strictly after the cursor in this total order"
 * can never silently drop or repeat a row, regardless of how many events
 * share the same createdAt.
 */
public class EventStore {
    private final List<Event> events = new ArrayList<>();

    public void insert(Event e) {
        events.add(e);
    }

    /**
     * Returns the next page strictly after (cursorCreatedAt, cursorId) in the
     * (createdAt DESC, id DESC) total order (null cursor means "start from
     * the newest").
     */
    public Page getPage(Long cursorCreatedAt, Integer cursorId, int limit) {
        List<Event> sorted = new ArrayList<>(events);
        sorted.sort(Comparator.comparingLong((Event e) -> e.createdAt).reversed()
                .thenComparing((Event e) -> e.id, Comparator.reverseOrder()));

        List<Event> page = new ArrayList<>();
        for (Event e : sorted) {
            if (cursorCreatedAt != null && !isAfterCursor(e, cursorCreatedAt, cursorId)) {
                continue;
            }
            page.add(e);
            if (page.size() == limit) {
                break;
            }
        }
        if (page.isEmpty()) {
            return new Page(page, cursorCreatedAt, cursorId);
        }
        Event last = page.get(page.size() - 1);
        return new Page(page, last.createdAt, last.id);
    }

    private boolean isAfterCursor(Event e, long cursorCreatedAt, int cursorId) {
        if (e.createdAt != cursorCreatedAt) {
            return e.createdAt < cursorCreatedAt;
        }
        return e.id < cursorId; // tie on createdAt: broken by the unique id, never by chance
    }

    public static class Page {
        public final List<Event> events;
        public final Long nextCursorCreatedAt;
        public final Integer nextCursorId;

        public Page(List<Event> events, Long nextCursorCreatedAt, Integer nextCursorId) {
            this.events = events;
            this.nextCursorCreatedAt = nextCursorCreatedAt;
            this.nextCursorId = nextCursorId;
        }
    }
}
