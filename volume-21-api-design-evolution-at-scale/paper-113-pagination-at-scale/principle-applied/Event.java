/**
 * A single feed item. Two events can legitimately share the exact same
 * "createdAt" value; "id" is a unique, monotonic tie-breaker assigned at
 * insert time (an auto-increment primary key, in a real database).
 */
public class Event {
    public final int id;
    public final long createdAt;

    public Event(int id, long createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
