import java.util.ArrayDeque;
import java.util.Deque;

// Overcorrection: the same "pool it, don't allocate fresh" technique that
// genuinely helped the large buffer, applied here to an object cheap enough
// that allocating it was never the problem. A real, thread-safe pool needs
// synchronization to be safely shared -- and that synchronization now runs
// on every single acquire and release, for an object trivial enough that
// this overhead can end up costing more than the allocation it replaces.
class SmallObjectPool {
    private final Deque<SmallObject> pool = new ArrayDeque<>();

    synchronized SmallObject acquire() {
        SmallObject obj = pool.poll();
        return (obj != null) ? obj : new SmallObject();
    }

    synchronized void release(SmallObject obj) {
        pool.push(obj);
    }
}
