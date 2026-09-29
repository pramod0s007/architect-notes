import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A feed ordered newest-first, paginated with plain OFFSET/LIMIT -- the
 * pagination scheme every SQL tutorial teaches first. A page is simply
 * "skip this many rows, then take this many," recomputed from scratch,
 * against whatever the live, current row order happens to be at the
 * moment each page is requested.
 */
public class EventStore {
    private final List<Event> events = new ArrayList<>();

    public void insert(Event e) {
        events.add(e);
    }

    /** Returns events sorted newest-first, skipping "offset" rows, taking "limit" rows. */
    public List<Event> getPage(int offset, int limit) {
        List<Event> sorted = new ArrayList<>(events);
        sorted.sort(Comparator.comparingLong((Event e) -> e.createdAt).reversed());
        List<Event> page = new ArrayList<>();
        for (int i = offset; i < Math.min(offset + limit, sorted.size()); i++) {
            page.add(sorted.get(i));
        }
        return page;
    }
}
