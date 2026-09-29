/**
 * A single feed item. Two events can legitimately share the exact same
 * "createdAt" value -- e.g. a batch import, or a clock with millisecond
 * resolution processing more than one event per millisecond, both totally
 * ordinary in a real system.
 */
public class Event {
    public final int id;
    public final long createdAt;

    public Event(int id, long createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
