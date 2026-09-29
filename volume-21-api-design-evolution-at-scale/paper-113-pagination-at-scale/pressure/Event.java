/**
 * A single feed item. "createdAt" is a monotonically increasing insertion
 * timestamp -- newer events have strictly larger values than every event
 * already in the store, exactly like an auto-increment id or a real clock.
 */
public class Event {
    public final int id;
    public final long createdAt;

    public Event(int id, long createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
